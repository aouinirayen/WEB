import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ReservationService } from '../../../core/services/reservation.service';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { UserSearchService, UserSuggestion } from '../../../core/services/user-search.service';
import { Reservation } from '../../../core/models/reservation.model';
import { MapPickerComponent } from '../../../shared/map-picker/map-picker';
import { AuthService } from '../../../services/auth/auth.service';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-user-reservation-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MapPickerComponent],
  templateUrl: './reservation-form.html',
  styleUrl: './reservation-form.scss'
})
export class ReservationFormComponent implements OnInit, OnDestroy {

  reservation: Reservation = {
    clientName: '', phone: '', email: '', seatsReserved: 1, bookingDate: '', status: 'PENDING'
  };
  covoiturageInfo = '';
  today = new Date().toISOString().split('T')[0];
  success = false;
  error = false;
  submitting = false;
  maxSeats = 8;

  // Autocomplete
  userSuggestions: UserSuggestion[] = [];
  phoneSuggestions: UserSuggestion[] = [];
  showSuggestions = false;
  showPhoneSuggestions = false;
  private nameSearch$ = new Subject<string>();
  private phoneSearch$ = new Subject<string>();
  private nameSub!: Subscription;
  private phoneSub!: Subscription;

  // Geolocation
  geoLoading = false;
  geoError = '';

  constructor(
    private reservationService: ReservationService,
    private covoiturageService: CovoiturageService,
    private userSearchService: UserSearchService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    // Pre-fill from logged-in user
    const user = this.authService.currentUserValue;
    if (user) {
      this.reservation.clientName = user.name || '';
      this.reservation.email = user.email || '';
      this.reservation.phone = user.phone || '';
    }

    this.route.queryParams.subscribe(params => {
      if (params['covoiturageId']) {
        this.reservation.covoiturageId = +params['covoiturageId'];
        this.covoiturageInfo = `${params['departure']} → ${params['destination']} (${params['date']}) - ${params['driverName']}`;
        this.covoiturageService.getById(this.reservation.covoiturageId).subscribe(c => {
          this.maxSeats = c.availableSeats;
          this.reservation.bookingDate = c.date;
          this.cdr.detectChanges();
        });
      }
    });

    // Debounced autocomplete for name (300ms)
    this.nameSub = this.nameSearch$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => this.userSearchService.search(q))
    ).subscribe(results => {
      this.userSuggestions = results;
      this.showSuggestions = results.length > 0;
      this.cdr.detectChanges();
    });

    // Debounced autocomplete for phone (300ms)
    this.phoneSub = this.phoneSearch$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => this.userSearchService.search(q))
    ).subscribe(results => {
      this.phoneSuggestions = results;
      this.showPhoneSuggestions = results.length > 0;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.nameSub?.unsubscribe();
    this.phoneSub?.unsubscribe();
  }

  // Autocomplete methods
  onNameInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.nameSearch$.next(value);
  }

  onPhoneInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.phoneSearch$.next(value);
  }

  selectUser(user: UserSuggestion): void {
    this.reservation.clientName = user.name;
    this.reservation.phone = user.phone;
    if (user.email) this.reservation.email = user.email;
    this.showSuggestions = false;
    this.showPhoneSuggestions = false;
    this.userSuggestions = [];
    this.phoneSuggestions = [];
  }

  // Geolocation
  useMyLocation(): void {
    this.geoError = '';
    this.geoLoading = true;

    if (!navigator.geolocation) {
      this.geoError = 'Geolocation is not supported by your browser.';
      this.geoLoading = false;
      this.cdr.detectChanges();
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        this.reservation.clientLat = position.coords.latitude;
        this.reservation.clientLng = position.coords.longitude;
        // Reverse geocoding via Nominatim
        fetch(`https://nominatim.openstreetmap.org/reverse?lat=${position.coords.latitude}&lon=${position.coords.longitude}&format=json`)
          .then(res => res.json())
          .then(data => {
            if (data.display_name) {
              this.reservation.clientAddress = data.display_name;
            } else {
              this.reservation.clientAddress = `${position.coords.latitude.toFixed(4)}, ${position.coords.longitude.toFixed(4)}`;
            }
            this.geoLoading = false;
            this.cdr.detectChanges();
          })
          .catch(() => {
            this.reservation.clientAddress = `${position.coords.latitude.toFixed(4)}, ${position.coords.longitude.toFixed(4)}`;
            this.geoLoading = false;
            this.cdr.detectChanges();
          });
      },
      (err) => {
        this.geoLoading = false;
        switch (err.code) {
          case err.PERMISSION_DENIED:
            this.geoError = 'Location permission denied. Please allow GPS access.';
            break;
          case err.POSITION_UNAVAILABLE:
            this.geoError = 'GPS position unavailable.';
            break;
          case err.TIMEOUT:
            this.geoError = 'GPS timeout exceeded.';
            break;
          default:
            this.geoError = 'Unknown geolocation error.';
        }
        this.cdr.detectChanges();
      },
      { enableHighAccuracy: true, timeout: 10000, maximumAge: 0 }
    );
  }

  onClientLocationSelected(event: { lat: number; lng: number }): void {
    this.reservation.clientLat = event.lat;
    this.reservation.clientLng = event.lng;
  }

  submit(): void {
    if (this.submitting) return;
    this.submitting = true;

    if (!this.reservation.bookingDate) {
      this.reservation.bookingDate = new Date().toISOString().split('T')[0];
    }

    if (!this.reservation.displacementRequested) {
      this.reservation.displacementPrice = undefined;
    }

    this.reservationService.create(this.reservation).subscribe({
      next: () => {
        if (this.reservation.covoiturageId) {
          this.covoiturageService.getById(this.reservation.covoiturageId).subscribe(cov => {
            cov.availableSeats = Math.max(0, cov.availableSeats - this.reservation.seatsReserved);
            this.covoiturageService.update(cov.id!, cov).subscribe();
          });
        }
        this.success = true;
        this.error = false;
        this.cdr.detectChanges();
        window.scrollTo({ top: 0, behavior: 'smooth' });
        setTimeout(() => this.router.navigate(['/ticket/reservations']), 3000);
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
