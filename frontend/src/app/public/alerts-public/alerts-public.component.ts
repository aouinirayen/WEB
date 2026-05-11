import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaintenanceService } from '../../services/maintenance.service';
import { MaintenanceOrder } from '../../models/models';

@Component({
  selector: 'app-alerts-public',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alerts-public.component.html'
})
export class AlertsPublicComponent implements OnInit {
  alerts: MaintenanceOrder[] = [];
  allOrders: MaintenanceOrder[] = [];

  constructor(private service: MaintenanceService) {}

  ngOnInit() {
    this.service.getAlerts().subscribe(data => this.alerts = data);
    this.service.getAll().subscribe(data => {
      this.allOrders = data.filter(o => o.status !== 'DONE');
    });
  }
}