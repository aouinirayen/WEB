import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FuelLogService } from '../../../services/fuel-log.service';
import { FuelLog } from '../../../models/models';

@Component({
  selector: 'app-fuel-log-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './fuel-log-list.component.html'
})
export class FuelLogListComponent implements OnInit {
  fuelLogs: FuelLog[] = [];
  totalCost = 0;

  constructor(private service: FuelLogService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => {
      this.fuelLogs = data;
      this.totalCost = data.reduce((sum, f) => sum + f.totalCost, 0);
    });
  }

  delete(id: number) {
    if (confirm('Delete this fuel log?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}
