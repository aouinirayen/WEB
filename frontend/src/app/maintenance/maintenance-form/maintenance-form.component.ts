import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { MaintenanceService } from '../../services/maintenance.service';
import { VehicleService } from '../../services/vehicle.service';
import { MaintenanceOrder, Vehicle } from '../../models/models';
import { PartUsagePanelComponent } from '../../metiers/part-usage/part-usage-panel.component';

@Component({
  selector: 'app-maintenance-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink,PartUsagePanelComponent],
  templateUrl: './maintenance-form.component.html'
})
export class MaintenanceFormComponent implements OnInit {
  order: MaintenanceOrder = {
    vehicle: { id: 0, plateNumber: '', brand: '', capacity: 0,
               mileage: 0, type: 'BUS', status: 'ACTIVE', purchaseDate: '' },
    type: 'PREVENTIVE',
    status: 'PENDING',
    scheduledDate: '',
    description: '',
    technicianName: '',
    cost: 0
  };
  vehicles: Vehicle[] = [];
  isEdit = false;
  id!: number;
  selectedVehicleId!: number;

  constructor(
    private service: MaintenanceService,
    private vehicleService: VehicleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.vehicleService.getAll().subscribe(data => {
      this.vehicles = data;
    });
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => {
        this.order = data;
        this.selectedVehicleId = data.vehicle.id!;
      });
    }
  }

  onVehicleChange() {
    const v = this.vehicles.find(v => v.id == this.selectedVehicleId);
    if (v) this.order.vehicle = v;
  }

  save() {
    if (!this.selectedVehicleId || !this.order.scheduledDate) return;
    if (!this.order.technicianName?.trim()) return;
    if ((this.order.cost ?? 0) < 0) return;

    if (this.isEdit) {
      this.service.update(this.id, this.order)
        .subscribe(() => this.router.navigate(['/admin/maintenance']));
    } else {
      this.service.create(this.order)
        .subscribe(() => this.router.navigate(['/admin/maintenance']));
    }
  }
}