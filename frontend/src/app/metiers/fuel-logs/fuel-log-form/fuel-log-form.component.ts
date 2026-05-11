import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { FuelLogService } from '../../../services/fuel-log.service';
import { VehicleService } from './../../../services/vehicle.service';
import { Driver, FuelLog, Vehicle } from '../../../models/models';
@Component({
  selector: 'app-fuel-log-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './fuel-log-form.component.html'
})
export class FuelLogFormComponent implements OnInit {
  fuelLog: FuelLog = {
    vehicle: {} as Vehicle,
    liters: 0,
    costPerLiter: 0,
    totalCost: 0,
    mileageAtFillUp: 0,
    fuelDate: ''
  };
  vehicles: Vehicle[] = [];
  isEdit = false;
  id!: number;

  constructor(
    private service: FuelLogService,
    private vehicleService: VehicleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.vehicleService.getAll().subscribe(data => this.vehicles = data);
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => this.fuelLog = data);
    }
  }

  recalcTotal() {
    this.fuelLog.totalCost = +(this.fuelLog.liters * this.fuelLog.costPerLiter).toFixed(3);
  }

  compareById(a: any, b: any): boolean {
    return a && b && a.id === b.id;
  }

  save() {
    if (!this.fuelLog.vehicle?.id || !this.fuelLog.fuelDate) return;
    if (this.fuelLog.liters <= 0 || this.fuelLog.costPerLiter <= 0) return;
    if ((this.fuelLog.mileageAtFillUp ?? 0) < 0) return;

    if (this.isEdit) {
      this.service.update(this.id, this.fuelLog)
        .subscribe(() => this.router.navigate(['/admin/fuel-logs']));
    } else {
      this.service.create(this.fuelLog)
        .subscribe(() => this.router.navigate(['/admin/fuel-logs']));
    }
  }
}
