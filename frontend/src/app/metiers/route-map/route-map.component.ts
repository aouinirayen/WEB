import { Component, OnInit, OnDestroy, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RoutingService, RouteResult, VehiclePosition } from '../../services/routing.service';
import { LineService } from '../../services/line.service';
import { Line } from '../../models/models';
import { interval, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import * as L from 'leaflet';

@Component({
  selector: 'app-route-map',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './route-map.component.html',
  styleUrls: ['./route-map.component.css']
})
export class RouteMapComponent implements OnInit, AfterViewInit, OnDestroy {

  lines: Line[]             = [];
  selectedLineId: number | null = null;
  routeResult: RouteResult | null = null;
  positions: VehiclePosition[]    = [];
  optimizing = false;

  // Real GPS
  realGpsActive  = false;
  realGpsVehicleId: number | null = null;
  private gpsWatchId: number | null = null;

  // Leaflet
  private map!: L.Map;
  private routeLayer!: L.LayerGroup;
  private vehicleLayer!: L.LayerGroup;
  private vehicleMarkers = new Map<number, L.Marker>();

  // Subscriptions
  private pollSub!: Subscription;

  constructor(
    private routingService: RoutingService,
    private lineService: LineService
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(d => this.lines = d);
    this.startPolling();
  }

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    this.pollSub?.unsubscribe();
    this.stopRealGps();
  }

  // ── Map init ──────────────────────────────────────────────────

  private initMap() {
    this.map = L.map('routeMap').setView([36.8065, 10.1815], 11); // Tunis center

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors',
      maxZoom: 18
    }).addTo(this.map);

    this.routeLayer  = L.layerGroup().addTo(this.map);
    this.vehicleLayer = L.layerGroup().addTo(this.map);
  }

  // ── Route optimization ────────────────────────────────────────

  onLineSelected() { this.routeResult = null; }

  optimizeRoute() {
    if (!this.selectedLineId) return;
    this.optimizing = true;

    this.routingService.optimizeRoute(this.selectedLineId).subscribe({
      next: result => {
        this.routeResult = result;
        this.optimizing  = false;
        this.drawRoute(result);
      },
      error: () => { this.optimizing = false; }
    });
  }

  private drawRoute(result: RouteResult) {
    this.routeLayer.clearLayers();

    const stops = result.optimalRoute;
    if (!stops.length) return;

    // Draw polyline
    const latlngs: L.LatLngTuple[] = stops.map(s => [s.latitude, s.longitude]);
    const color = result.method === 'WEIGHTED_AI' ? '#3b82f6' : '#22c55e';

    L.polyline(latlngs, { color, weight: 5, opacity: 0.85 })
      .addTo(this.routeLayer);

    // Draw stop markers
    stops.forEach((stop, i) => {
      const isFirst = i === 0;
      const isLast  = i === stops.length - 1;

      const icon = L.divIcon({
        html: `<div class="stop-marker ${isFirst ? 'stop-start' : isLast ? 'stop-end' : ''}">${stop.sequence}</div>`,
        className: '',
        iconSize: [28, 28],
        iconAnchor: [14, 14]
      });

      L.marker([stop.latitude, stop.longitude], { icon })
        .bindPopup(`
          <b>${stop.name}</b><br>
          Stop #${stop.sequence}<br>
          Road: ${stop.roadType}<br>
          Dist from prev: ${stop.distanceFromPrevKm.toFixed(2)} km
        `)
        .addTo(this.routeLayer);
    });

    // Fit map to route
    this.map.fitBounds(L.latLngBounds(latlngs), { padding: [30, 30] });
  }

  // ── Live vehicle markers ──────────────────────────────────────

  private startPolling() {
    // Load immediately then poll every 10s
    this.loadPositions();
    this.pollSub = interval(10000)
      .pipe(switchMap(() => this.routingService.getAllPositions()))
      .subscribe(data => {
        this.positions = data;
        this.updateVehicleMarkers(data);
      });
  }

  private loadPositions() {
    this.routingService.getAllPositions().subscribe(data => {
      this.positions = data;
      this.updateVehicleMarkers(data);
    });
  }

  private updateVehicleMarkers(positions: VehiclePosition[]) {
    positions.forEach(pos => {
      const vehicleId = pos.vehicle?.id;
      if (!vehicleId) return;
      if (!pos.latitude || !pos.longitude) return;
      const latlng: L.LatLngTuple = [pos.latitude, pos.longitude];

      if (this.vehicleMarkers.has(vehicleId)) {
        // Smoothly move existing marker
        this.vehicleMarkers.get(vehicleId)!.setLatLng(latlng);
      } else {
        // Create new vehicle marker
        const icon = L.divIcon({
          html: `<div class="vehicle-marker ${pos.simulated ? 'v-sim' : 'v-real'}">🚍</div>`,
          className: '',
          iconSize: [32, 32],
          iconAnchor: [16, 16]
        });

        const marker = L.marker(latlng, { icon })
          .bindPopup(`
            <b>${pos.vehicle?.plateNumber}</b><br>
            Speed: ${pos.speedKmh?.toFixed(0)} km/h<br>
            Heading: ${pos.heading}<br>
            ${pos.simulated ? '🔄 Simulated' : '📡 Real GPS'}<br>
            Updated: ${pos.updatedAt?.substring(0, 16)}
          `)
          .addTo(this.vehicleLayer);

        this.vehicleMarkers.set(vehicleId, marker);
      }
    });
  }

  // ── Real GPS ──────────────────────────────────────────────────

  startRealGps() {
    if (!navigator.geolocation) {
      alert('Geolocation is not supported by your browser.');
      return;
    }

    // Use first vehicle as the "real GPS" vehicle
    if (this.positions.length === 0) {
      alert('No vehicles loaded yet.');
      return;
    }

    const firstVehicleId = this.positions[0]?.vehicle?.id;
    if (!firstVehicleId) {
      alert('No valid vehicle found for GPS tracking.');
      return;
    }

    this.realGpsVehicleId = firstVehicleId;
    this.realGpsActive    = true;

    this.gpsWatchId = navigator.geolocation.watchPosition(
      pos => {
        const { latitude, longitude } = pos.coords;
        this.routingService.updateRealGps(this.realGpsVehicleId!, latitude, longitude)
          .subscribe();
      },
      err  => console.error('GPS error:', err),
      { enableHighAccuracy: true, maximumAge: 5000 }
    );
  }

  stopRealGps() {
    if (this.gpsWatchId !== null) {
      navigator.geolocation.clearWatch(this.gpsWatchId);
      this.gpsWatchId = null;
    }
    this.realGpsActive = false;
  }
}
