import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TripService } from '../../services/trip.service';
import { Trip } from '../../models/models';

@Component({
  selector: 'app-trip-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './trip-list.component.html'
})
export class TripListComponent implements OnInit {
  trips: Trip[] = [];

  constructor(private service: TripService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.trips = data);
  }

  delete(id: number) {
    if (confirm('Delete this trip?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}