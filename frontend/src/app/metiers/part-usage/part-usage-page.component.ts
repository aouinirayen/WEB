import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PartUsagePanelComponent } from './part-usage-panel.component';
import { MaintenanceService } from '../../services/maintenance.service';
import { MaintenanceOrder } from '../../models/models';

@Component({
  selector: 'app-part-usage-page',
  standalone: true,
  imports: [CommonModule, FormsModule, PartUsagePanelComponent],
  templateUrl: './part-usage-page.component.html'
})
export class PartUsagePageComponent implements OnInit {
  maintenanceOrderId: number | null = null;
  maintenanceType = 'CORRECTIVE';
  maintenanceOrders: MaintenanceOrder[] = [];

  constructor(private readonly maintenanceService: MaintenanceService) {}

  ngOnInit(): void {
    this.maintenanceService.getAll().subscribe(data => {
      this.maintenanceOrders = data ?? [];
    });
  }

  onOrderChange(): void {
    const selected = this.maintenanceOrders.find(o => o.id === this.maintenanceOrderId);
    if (selected?.type) {
      this.maintenanceType = selected.type;
    }
  }
}
