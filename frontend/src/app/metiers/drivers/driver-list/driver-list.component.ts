import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DriverService, Driver } from '../../../services/driver.service';
import { VehicleService } from '../../../services/vehicle.service';
import { Vehicle } from '../../../models/models';

@Component({
  selector: 'app-driver-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './driver-list.component.html'
})
export class DriverListComponent implements OnInit {
  drivers: Driver[] = [];
  vehicles: Vehicle[] = [];
  assigning: number | null = null;
  message = '';
  messageType = '';

  constructor(
    private driverService: DriverService,
    private vehicleService: VehicleService
  ) {}

  ngOnInit() {
    this.load();
    this.vehicleService.getAll().subscribe(data => this.vehicles = data);
  }

  load() {
    this.driverService.getAll().subscribe(data => this.drivers = data);
  }

  delete(id: number) {
    if (confirm('Delete this driver?')) {
      this.driverService.delete(id).subscribe(() => this.load());
    }
  }

  autoAssign(vehicleId: number) {
    this.assigning = vehicleId;
    this.driverService.autoAssign(vehicleId).subscribe({
      next: (driver) => {
        this.message = `✓ ${driver.firstName} ${driver.lastName}
          successfully assigned!`;
        this.messageType = 'success';
        this.assigning = null;
        this.load();
      },
      error: (err) => {
        this.message = '✗ No available driver found for this vehicle type.';
        this.messageType = 'danger';
        this.assigning = null;
      }
    });
  }

  unassign(id: number) {
    this.driverService.unassign(id).subscribe(() => this.load());
  }
}