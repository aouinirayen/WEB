import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { DriverService, Driver } from '../../../services/driver.service';

@Component({
  selector: 'app-driver-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './driver-form.component.html'
})
export class DriverFormComponent implements OnInit {
  driver: Driver = {
    firstName: '',
    lastName: '',
    phone: '',
    licenseNumber: '',
    licenseType: 'C',
    licenseExpiryDate: '',
    experienceYears: 0
  };
  isEdit = false;
  id!: number;
  private readonly phonePattern = /^\+216\s?\d{2}\s?\d{3}\s?\d{3}$/;
  private readonly licensePattern = /^[0-9]{8}$/;

  constructor(
    private service: DriverService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => {
        this.driver = data;
        this.normalizeLicenseNumber(this.driver.licenseNumber);
      });
    } else {
      this.driver.phone = '+216 ';
    }
  }

  normalizePhone(value: string): void {
    const raw = (value ?? '').replace(/[^\d+]/g, '');
    let local = raw;
    if (local.startsWith('+216')) {
      local = local.slice(4);
    } else if (local.startsWith('216')) {
      local = local.slice(3);
    }

    const digits = local.replace(/\D/g, '').slice(0, 8);
    if (!digits) {
      this.driver.phone = '+216 ';
      return;
    }

    if (digits.length <= 2) {
      this.driver.phone = `+216 ${digits}`;
    } else if (digits.length <= 5) {
      this.driver.phone = `+216 ${digits.slice(0, 2)} ${digits.slice(2)}`;
    } else {
      this.driver.phone = `+216 ${digits.slice(0, 2)} ${digits.slice(2, 5)} ${digits.slice(5)}`;
    }
  }

  ensurePhonePrefix(): void {
    if (!this.driver.phone || !this.driver.phone.trim()) {
      this.driver.phone = '+216 ';
    } else if (!this.driver.phone.startsWith('+216')) {
      this.normalizePhone(this.driver.phone);
    }
  }

  normalizeLicenseNumber(value: string): void {
    this.driver.licenseNumber = (value ?? '')
      .replace(/\D/g, '')
      .slice(0, 8);
  }

  save() {
    if (!this.driver.firstName?.trim() || !this.driver.lastName?.trim()) return;
    if (!this.phonePattern.test(this.driver.phone ?? '')) return;
    if (!this.driver.licenseNumber?.trim() || !this.driver.licenseExpiryDate) return;
    if (!this.licensePattern.test(this.driver.licenseNumber ?? '')) return;
    if ((this.driver.experienceYears ?? 0) < 0) return;

    if (this.isEdit) {
      this.service.update(this.id, this.driver)
        .subscribe(() => this.router.navigate(['/admin/drivers']));
    } else {
      this.service.create(this.driver)
        .subscribe(() => this.router.navigate(['/admin/drivers']));
    }
  }
}