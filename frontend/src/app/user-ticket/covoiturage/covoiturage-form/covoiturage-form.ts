import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { Covoiturage } from '../../../core/models/covoiturage.model';
import { MapPickerComponent } from '../../../shared/map-picker/map-picker';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-user-covoiturage-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MapPickerComponent],
  templateUrl: './covoiturage-form.html',
  styleUrl: './covoiturage-form.scss'
})
export class CovoiturageFormComponent {

  covoiturage: Covoiturage = {
    driverName: '', departure: '', destination: '', date: '',
    heureDepart: '', heureArrivee: '',
    price: 0, availableSeats: 1, vehicle: '', status: 'PENDING'
  };
  get today(): string {
    const d = new Date();
    return d.getFullYear() + '-' +
      String(d.getMonth() + 1).padStart(2, '0') + '-' +
      String(d.getDate()).padStart(2, '0');
  }

  get minTime(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 30);
    return now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');
  }
  success = false;
  error = false;
  submitting = false;

  // Price estimation
  private lastDepLat = 0;
  private lastDepLng = 0;
  private lastDestLat = 0;
  private lastDestLng = 0;
  distanceKm = 0;
  durationMin = 0;
  durationNoTrafficMin = 0;
  trafficDelayMin = 0;
  avgSpeedKmh = 0;
  routeType = '';
  trafficLevel = '';
  estimatedPriceMin = 0;
  estimatedPriceMax = 0;
  estimatingPrice = false;
  estimationError = '';
  isLiveTraffic = false;
  isPredictive = false;
  estimatedArrivalTime = '';

  private geocodeDepTimeout: any;
  private geocodeDestTimeout: any;

  constructor(
    private covoiturageService: CovoiturageService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  get isHeureDepartInvalid(): boolean {
    if (!this.covoiturage.heureDepart || !this.covoiturage.date) return false;
    if (this.covoiturage.date > this.today) return false;
    if (this.covoiturage.date < this.today) return true;
    return this.covoiturage.heureDepart < this.minTime;
  }

  onDepartureChanged(): void {
    clearTimeout(this.geocodeDepTimeout);
    if (this.covoiturage.departure && this.covoiturage.departure.length >= 3) {
      this.geocodeDepTimeout = setTimeout(() => this.geocode(this.covoiturage.departure, 'departure'), 800);
    }
  }

  onDestinationChanged(): void {
    clearTimeout(this.geocodeDestTimeout);
    if (this.covoiturage.destination && this.covoiturage.destination.length >= 3) {
      this.geocodeDestTimeout = setTimeout(() => this.geocode(this.covoiturage.destination, 'destination'), 800);
    }
  }

  onDepartureLocationSelected(event: { lat: number; lng: number }): void {
    this.covoiturage.departureLat = event.lat;
    this.covoiturage.departureLng = event.lng;
    this.tryEstimatePrice();
  }

  onSeatsChanged(): void {
    if (this.distanceKm > 0) {
      this.calculatePrice();
    }
  }

  onDateTimeChanged(): void {
    // Only re-fetch traffic duration, not distance
    this.tryEstimatePrice(true);
  }

  private geocode(query: string, type: 'departure' | 'destination'): void {
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&countrycodes=tn`;
    fetch(url)
      .then(res => res.json())
      .then((data: any[]) => {
        if (data && data.length > 0) {
          const lat = parseFloat(data[0].lat);
          const lng = parseFloat(data[0].lon);
          if (type === 'departure') {
            this.covoiturage.departureLat = lat;
            this.covoiturage.departureLng = lng;
          } else {
            this.covoiturage.destinationLat = lat;
            this.covoiturage.destinationLng = lng;
          }
          this.cdr.detectChanges();
          this.tryEstimatePrice();
        }
      })
      .catch(() => {});
  }

  private tryEstimatePrice(dateTimeOnly = false): void {
    if (this.covoiturage.departureLat && this.covoiturage.departureLng &&
        this.covoiturage.destinationLat && this.covoiturage.destinationLng) {

      const coordsChanged =
        this.covoiturage.departureLat !== this.lastDepLat ||
        this.covoiturage.departureLng !== this.lastDepLng ||
        this.covoiturage.destinationLat !== this.lastDestLat ||
        this.covoiturage.destinationLng !== this.lastDestLng;

      // Save coordinates
      this.lastDepLat = this.covoiturage.departureLat!;
      this.lastDepLng = this.covoiturage.departureLng!;
      this.lastDestLat = this.covoiturage.destinationLat!;
      this.lastDestLng = this.covoiturage.destinationLng!;

      const apiKey = environment.tomtomApiKey;
      if (apiKey && apiKey !== 'YOUR_TOMTOM_API_KEY') {
        this.fetchTomTomRoute(apiKey, dateTimeOnly && !coordsChanged);
      } else {
        this.fetchOsrmFallback(dateTimeOnly && !coordsChanged);
      }
    }
  }

  private fetchTomTomRoute(apiKey: string, keepDistance = false): void {
    this.estimatingPrice = true;
    this.estimationError = '';
    this.cdr.detectChanges();

    const depLat = this.covoiturage.departureLat;
    const depLng = this.covoiturage.departureLng;
    const destLat = this.covoiturage.destinationLat;
    const destLng = this.covoiturage.destinationLng;

    // Determine departAt: 'now' for today, future date/time for predictive
    let departAt = 'now';
    const selectedDate = this.covoiturage.date;
    const selectedTime = this.covoiturage.heureDepart;

    if (selectedDate && selectedDate > this.today) {
      // Future date: use TomTom historical traffic prediction
      const time = selectedTime || '08:00';
      departAt = `${selectedDate}T${time}:00`;
      this.isLiveTraffic = false;
      this.isPredictive = true;
    } else if (selectedDate && selectedDate === this.today && selectedTime) {
      // Today with specific time in the future
      departAt = `${selectedDate}T${selectedTime}:00`;
      this.isLiveTraffic = true;
      this.isPredictive = false;
    } else {
      // No date or today without time: use real-time
      this.isLiveTraffic = true;
      this.isPredictive = false;
    }

    const url = `https://api.tomtom.com/routing/1/calculateRoute/${depLat},${depLng}:${destLat},${destLng}/json?key=${apiKey}&traffic=true&travelMode=car&departAt=${departAt}`;

    console.log('[TomTom] Calling:', url.replace(apiKey, '***'));

    fetch(url)
      .then(res => {
        if (!res.ok) {
          return res.text().then(body => {
            console.error('[TomTom] HTTP Error:', res.status, body);
            throw new Error(`TomTom HTTP ${res.status}`);
          });
        }
        return res.json();
      })
      .then((data: any) => {
        this.estimatingPrice = false;
        console.log('[TomTom] Response OK, routes:', data.routes?.length);
        if (data.routes && data.routes.length > 0) {
          const summary = data.routes[0].summary;
          if (!keepDistance || this.distanceKm === 0) {
            this.distanceKm = Math.round(summary.lengthInMeters / 1000 * 10) / 10;
          }
          this.durationMin = Math.round(summary.travelTimeInSeconds / 60);
          this.durationNoTrafficMin = Math.round(summary.noTrafficTravelTimeInSeconds / 60);
          this.trafficDelayMin = Math.round(summary.trafficDelayInSeconds / 60);
          console.log('[TomTom] Distance:', this.distanceKm, 'km, Duration:', this.durationMin, 'min (no traffic:', this.durationNoTrafficMin, 'min), Delay:', this.trafficDelayMin, 'min');
          this.calculatePrice();
          this.detectTrafficLevel();
          this.autoFillArrivalTime();
        } else {
          this.estimationError = 'No route found.';
        }
        this.cdr.detectChanges();
      })
      .catch((err) => {
        console.error('[TomTom] Failed, falling back to OSRM:', err.message);
        this.isLiveTraffic = false;
        this.isPredictive = false;
        this.fetchOsrmFallback();
      });
  }

  private fetchOsrmFallback(keepDistance = false): void {
    this.estimatingPrice = true;
    this.estimationError = '';
    this.isLiveTraffic = false;
    this.cdr.detectChanges();

    const depLng = this.covoiturage.departureLng;
    const depLat = this.covoiturage.departureLat;
    const destLng = this.covoiturage.destinationLng;
    const destLat = this.covoiturage.destinationLat;

    const url = `https://router.project-osrm.org/route/v1/driving/${depLng},${depLat};${destLng},${destLat}?overview=false`;

    fetch(url)
      .then(res => res.json())
      .then((data: any) => {
        this.estimatingPrice = false;
        if (data.code === 'Ok' && data.routes && data.routes.length > 0) {
          if (!keepDistance || this.distanceKm === 0) {
            this.distanceKm = Math.round(data.routes[0].distance / 1000 * 10) / 10;
          }
          this.durationMin = Math.round(data.routes[0].duration / 60);
          this.durationNoTrafficMin = this.durationMin;
          this.trafficDelayMin = 0;
          this.calculatePrice();
          this.autoFillArrivalTime();
        } else {
          this.estimationError = 'Unable to calculate distance.';
        }
        this.cdr.detectChanges();
      })
      .catch(() => {
        this.estimatingPrice = false;
        this.estimationError = 'Connection error.';
        this.cdr.detectChanges();
      });
  }

  private detectTrafficLevel(): void {
    if (this.durationNoTrafficMin === 0) {
      this.trafficLevel = 'Unknown';
      return;
    }
    const ratio = this.durationMin / this.durationNoTrafficMin;
    if (ratio < 1.15) {
      this.trafficLevel = 'Light';
    } else if (ratio < 1.4) {
      this.trafficLevel = 'Moderate';
    } else if (ratio < 1.8) {
      this.trafficLevel = 'Heavy';
    } else {
      this.trafficLevel = 'Gridlock';
    }
  }

  private calculatePrice(): void {
    // Detect route type from average speed (OSRM)
    this.avgSpeedKmh = this.durationMin > 0 ? Math.round(this.distanceKm / (this.durationMin / 60)) : 0;

    // Tunisian carpooling market rates (TND/km per seat)
    // Calibrated: louage ~12 TND for 140km, bus ~13 TND
    let rateMin: number;
    let rateMax: number;

    if (this.avgSpeedKmh < 35) {
      // Urbain - embouteillage
      this.routeType = 'Urban (congested)';
      rateMin = 0.15;
      rateMax = 0.30;
    } else if (this.avgSpeedKmh < 50) {
      // Periurbain
      this.routeType = 'Suburban';
      rateMin = 0.10;
      rateMax = 0.20;
    } else if (this.avgSpeedKmh < 70) {
      // Mixte (route nationale)
      this.routeType = 'National road';
      rateMin = 0.07;
      rateMax = 0.14;
    } else {
      // Autoroute
      this.routeType = 'Highway';
      rateMin = 0.06;
      rateMax = 0.12;
    }

    this.estimatedPriceMin = Math.round(rateMin * this.distanceKm * 10) / 10;
    this.estimatedPriceMax = Math.round(rateMax * this.distanceKm * 10) / 10;

    // Minimum 1 TND
    this.estimatedPriceMin = Math.max(1, this.estimatedPriceMin);
    this.estimatedPriceMax = Math.max(this.estimatedPriceMin + 1, this.estimatedPriceMax);
  }

  private autoFillArrivalTime(): void {
    if (!this.covoiturage.heureDepart || this.durationMin <= 0) return;

    const [h, m] = this.covoiturage.heureDepart.split(':').map(Number);
    const departDate = new Date(2000, 0, 1, h, m);
    departDate.setMinutes(departDate.getMinutes() + this.durationMin);

    this.estimatedArrivalTime =
      departDate.getHours().toString().padStart(2, '0') + ':' +
      departDate.getMinutes().toString().padStart(2, '0');

    this.covoiturage.heureArrivee = this.estimatedArrivalTime;
  }

  get isHeureArriveeInvalid(): boolean {
    if (!this.covoiturage.heureArrivee || !this.estimatedArrivalTime) return false;
    return this.covoiturage.heureArrivee > this.estimatedArrivalTime;
  }

  applyEstimatedPrice(price: number): void {
    this.covoiturage.price = Math.round(price);
    this.cdr.detectChanges();
  }

  submit(): void {
    if (this.submitting || this.isHeureDepartInvalid || this.isHeureArriveeInvalid) return;
    this.submitting = true;
    this.covoiturage.status = 'PENDING';
    this.covoiturageService.create(this.covoiturage).subscribe({
      next: () => {
        this.success = true;
        this.error = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.error = true;
        this.success = false;
        this.submitting = false;
        this.cdr.detectChanges();
      }
    });
  }
}
