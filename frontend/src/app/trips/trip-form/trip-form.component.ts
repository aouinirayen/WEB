import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { TripService } from '../../services/trip.service';
import { ScheduleService } from '../../services/schedule.service';
import { Trip, Schedule } from '../../models/models';

@Component({
  selector: 'app-trip-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './trip-form.component.html'
})
export class TripFormComponent implements OnInit {
  trip: Trip = {
    schedule: { id: 0, line: { id: 0, name: '', code: '', mode: 'BUS', status: 'ACTIVE' },
                vehicle: { id: 0, plateNumber: '', brand: '', capacity: 0,
                           mileage: 0, type: 'BUS', status: 'ACTIVE', purchaseDate: '' },
                dayType: 'WEEKDAY', startTime: '', endTime: '', frequencyMinutes: 15 },
    departureTime: '',
    arrivalTime: '',
    delayMinutes: 0,
    completed: false
  };
  schedules: Schedule[] = [];
  isEdit = false;
  id!: number;
  selectedScheduleId!: number;

  constructor(
    private service: TripService,
    private scheduleService: ScheduleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.scheduleService.getAll().subscribe(data => this.schedules = data);
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => {
        this.trip = data;
        this.selectedScheduleId = data.schedule.id!;
      });
    }
  }

  onScheduleChange() {
    const s = this.schedules.find(s => s.id == this.selectedScheduleId);
    if (s) this.trip.schedule = s;
  }

  private isDateOrderValid(): boolean {
    return !!this.trip.departureTime &&
      !!this.trip.arrivalTime &&
      this.trip.arrivalTime > this.trip.departureTime;
  }

  save() {
    if (!this.selectedScheduleId) return;
    if (!this.isDateOrderValid()) return;
    if ((this.trip.delayMinutes ?? 0) < 0) return;

    if (this.isEdit) {
      this.service.update(this.id, this.trip)
        .subscribe(() => this.router.navigate(['/admin/trips']));
    } else {
      this.service.create(this.trip)
        .subscribe(() => this.router.navigate(['/admin/trips']));
    }
  }
}