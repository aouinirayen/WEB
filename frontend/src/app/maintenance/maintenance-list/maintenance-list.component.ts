import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MaintenanceService } from '../../services/maintenance.service';
import { MaintenanceOrder } from '../../models/models';

@Component({
  selector: 'app-maintenance-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './maintenance-list.component.html'
})
export class MaintenanceListComponent implements OnInit {
  orders: MaintenanceOrder[] = [];

  constructor(private service: MaintenanceService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.orders = data);
  }

  delete(id: number) {
    if (confirm('Delete this order?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}