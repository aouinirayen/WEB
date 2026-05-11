import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LineService } from '../../services/line.service';
import { TripService } from '../../services/trip.service';
import { ScheduleService } from '../../services/schedule.service';
import { Line, Trip, Schedule } from '../../models/models';

@Component({
  selector: 'app-trips-public',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './trips-public.component.html'
})
export class TripsPublicComponent implements OnInit {
  lines: Line[] = [];
  schedules: Schedule[] = [];
  trips: Trip[] = [];
  selectedLineId: number | null = null;
  selectedScheduleId: number | null = null;

  constructor(
    private lineService: LineService,
    private scheduleService: ScheduleService,
    private tripService: TripService
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(data => this.lines = data);
  }

  onLineSelect() {
    this.schedules = [];
    this.trips = [];
    this.selectedScheduleId = null;
    if (this.selectedLineId) {
      this.scheduleService.getByLine(this.selectedLineId)
        .subscribe(data => this.schedules = data);
    }
  }

  onScheduleSelect() {
    if (this.selectedScheduleId) {
      this.tripService.getBySchedule(this.selectedScheduleId)
        .subscribe(data => this.trips = data);
    }
  }
}