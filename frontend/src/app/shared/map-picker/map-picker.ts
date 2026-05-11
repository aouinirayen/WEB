import { Component, Input, Output, EventEmitter, AfterViewInit, OnChanges, SimpleChanges } from '@angular/core';
import * as L from 'leaflet';

@Component({
  selector: 'app-map-picker',
  standalone: true,
  template: `
    <div [id]="mapId" style="height:300px; border-radius:12px; border:2px solid #dee2e6; z-index:0;"></div>
    <div class="d-flex align-items-center mt-1">
      <small class="text-muted">
        <i class="fas fa-map-pin mr-1"></i>
        @if (!readonly) { Cliquez sur la carte pour selectionner }
        @if (lat && lng) { — <strong>{{ lat.toFixed(4) }}, {{ lng.toFixed(4) }}</strong> }
      </small>
      @if (searching) {
        <small class="ml-2 text-info"><i class="fas fa-spinner fa-spin mr-1"></i> Recherche...</small>
      }
      @if (geocodeError) {
        <small class="ml-2 text-danger"><i class="fas fa-exclamation-triangle mr-1"></i> Lieu introuvable</small>
      }
    </div>`,
  styles: [`:host { display: block; }`]
})
export class MapPickerComponent implements AfterViewInit, OnChanges {
  @Input() mapId = 'map';
  @Input() lat = 36.8065;
  @Input() lng = 10.1815;
  @Input() zoom = 13;
  @Input() readonly = false;
  @Input() searchQuery = '';
  @Output() locationSelected = new EventEmitter<{ lat: number; lng: number }>();

  searching = false;
  geocodeError = false;

  private map!: L.Map;
  private marker?: L.Marker;
  private initialized = false;
  private geocodeTimeout: any;

  private defaultIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  ngAfterViewInit(): void {
    setTimeout(() => this.initMap(), 100);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (this.initialized && (changes['lat'] || changes['lng']) && !changes['searchQuery']) {
      this.updateMarker();
    }
    if (changes['searchQuery'] && this.initialized) {
      const query = changes['searchQuery'].currentValue;
      if (query && query.length >= 3) {
        this.debouncedGeocode(query);
      }
    }
  }

  private debouncedGeocode(query: string): void {
    clearTimeout(this.geocodeTimeout);
    this.geocodeTimeout = setTimeout(() => this.geocode(query), 600);
  }

  private geocode(query: string): void {
    this.searching = true;
    this.geocodeError = false;
    const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(query)}&limit=1&countrycodes=tn`;
    fetch(url)
      .then(res => res.json())
      .then((data: any[]) => {
        this.searching = false;
        if (data && data.length > 0) {
          this.lat = parseFloat(data[0].lat);
          this.lng = parseFloat(data[0].lon);
          this.updateMarker();
          this.map.setView([this.lat, this.lng], 14);
          this.locationSelected.emit({ lat: this.lat, lng: this.lng });
          this.geocodeError = false;
        } else {
          this.geocodeError = true;
        }
      })
      .catch(() => {
        this.searching = false;
        this.geocodeError = true;
      });
  }

  private initMap(): void {
    this.map = L.map(this.mapId, {
      scrollWheelZoom: false,
      keyboard: false
    }).setView([this.lat, this.lng], this.zoom);
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; OpenStreetMap'
    }).addTo(this.map);

    if (this.lat && this.lng) {
      this.marker = L.marker([this.lat, this.lng], { icon: this.defaultIcon }).addTo(this.map);
    }

    if (!this.readonly) {
      this.map.on('click', (e: L.LeafletMouseEvent) => {
        this.lat = e.latlng.lat;
        this.lng = e.latlng.lng;
        this.updateMarker();
        this.locationSelected.emit({ lat: this.lat, lng: this.lng });
      });
    }

    this.initialized = true;

    setTimeout(() => this.map.invalidateSize(), 200);

    if (this.searchQuery && this.searchQuery.length >= 3) {
      this.geocode(this.searchQuery);
    }
  }

  private updateMarker(): void {
    if (this.marker) {
      this.marker.setLatLng([this.lat, this.lng]);
    } else {
      this.marker = L.marker([this.lat, this.lng], { icon: this.defaultIcon }).addTo(this.map);
    }
    this.map.setView([this.lat, this.lng], this.map.getZoom());
  }
}
