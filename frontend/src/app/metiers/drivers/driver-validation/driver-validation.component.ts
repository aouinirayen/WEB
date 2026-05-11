import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DriverService, Driver } from '../../../services/driver.service';

@Component({
  selector: 'app-driver-validation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './driver-validation.component.html'
})
export class DriverValidationComponent implements OnInit {
  pending: Driver[] = [];
  rejectionReasons: { [id: number]: string } = {};
  message = '';
  messageType = '';

  constructor(private service: DriverService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getPending().subscribe(data => this.pending = data);
  }

  approve(id: number) {
    this.service.approve(id).subscribe({
      next: () => {
        this.message = '✓ License approved successfully!';
        this.messageType = 'success';
        this.load();
      }
    });
  }

  reject(id: number) {
    const reason = this.rejectionReasons[id];
    if (!reason || reason.trim() === '') {
      alert('Please enter a rejection reason');
      return;
    }
    this.service.reject(id, reason).subscribe({
      next: () => {
        this.message = '✓ License rejected.';
        this.messageType = 'warning';
        this.load();
      }
    });
  }
}