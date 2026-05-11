import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { VehicleService } from '../../services/vehicle.service';
import { Vehicle } from '../../models/models';

@Component({
  selector: 'app-vehicle-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './vehicle-form.component.html'
})
export class VehicleFormComponent implements OnInit {
  vehicle: Vehicle = {
    plateNumber: '', brand: '', capacity: 0,
    mileage: 0, type: 'BUS', status: 'ACTIVE',
    purchaseDate: ''
  };
  isEdit = false;
  id!: number;
  private readonly platePattern = /^TN-[A-Z0-9-]{2,16}$/;
  private readonly brandPattern = /^[A-Za-z][A-Za-z0-9\s-]{1,49}$/;

  constructor(
    private service: VehicleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(
        data => this.vehicle = data
      );
    }
  }

  normalizePlateNumber(value: string): void {
    let cleaned = (value ?? '').toUpperCase().replace(/\s+/g, '').replace(/[^A-Z0-9-]/g, '');
    if (!cleaned) {
      this.vehicle.plateNumber = '';
      return;
    }

    cleaned = cleaned.replace(/^TN-?/, '');
    cleaned = `TN-${cleaned.replace(/^-+/, '')}`.replace(/--+/g, '-');
    this.vehicle.plateNumber = cleaned.slice(0, 20);
  }

  normalizeBrand(value: string): void {
    this.vehicle.brand = (value ?? '').replace(/\s+/g, ' ').trimStart();
  }

  save() {
    if (!this.vehicle.plateNumber?.trim() || !this.vehicle.brand?.trim()) return;
    if (this.vehicle.capacity <= 0 || this.vehicle.mileage < 0) return;
    if (!this.vehicle.purchaseDate) return;
    if (!this.platePattern.test(this.vehicle.plateNumber)) return;
    if (!this.brandPattern.test(this.vehicle.brand.trim())) return;

    if (this.isEdit) {
      this.service.update(this.id, this.vehicle)
        .subscribe(() => this.router.navigate(['/admin/vehicles']));
    } else {
      this.service.create(this.vehicle)
        .subscribe(() => this.router.navigate(['/admin/vehicles']));
    }
  }
}