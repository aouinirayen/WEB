import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { VehicleService } from '../../services/vehicle.service';
import { MaintenanceService } from '../../services/maintenance.service';
import { LineService } from '../../services/line.service';
import { TripService } from '../../services/trip.service';
import { Vehicle, MaintenanceOrder } from '../../models/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  today = new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' });

  stats = {
    totalVehicles: 0,
    activeVehicles: 0,
    totalLines: 0,
    activeLines: 0,
    pendingMaintenance: 0,
    totalTrips: 0
  };

  recentVehicles: Vehicle[] = [];
  pendingMaintenances: MaintenanceOrder[] = [];

  constructor(
    private vehicleService: VehicleService,
    private maintenanceService: MaintenanceService,
    private lineService: LineService,
    private tripService: TripService
  ) {}

  ngOnInit() {
    forkJoin({
      vehicles: this.vehicleService.getAll(),
      maintenance: this.maintenanceService.getAll(),
      lines: this.lineService.getAll(),
      trips: this.tripService.getAll()
    }).subscribe(data => {
      this.stats.totalVehicles = data.vehicles.length;
      this.stats.activeVehicles = data.vehicles.filter(v => v.status === 'ACTIVE').length;
      this.stats.totalLines = data.lines.length;
      this.stats.activeLines = data.lines.filter(l => l.status === 'ACTIVE').length;
      this.stats.pendingMaintenance = data.maintenance.filter(m => m.status === 'PENDING').length;
      this.stats.totalTrips = data.trips.length;

      this.recentVehicles = data.vehicles.slice(0, 4);
      this.pendingMaintenances = data.maintenance
        .filter(m => m.status === 'PENDING')
        .slice(0, 3);
    });
  }
}
