import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { VehicleService } from '../../services/vehicle.service';
import { Vehicle } from '../../models/models';

@Component({
  selector: 'app-vehicle-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vehicle-list.component.html'
})
export class VehicleListComponent implements OnInit {
  vehicles: Vehicle[] = [];

  constructor(private service: VehicleService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.vehicles = data);
  }

  delete(id: number) {
    if (confirm('Delete this vehicle?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}