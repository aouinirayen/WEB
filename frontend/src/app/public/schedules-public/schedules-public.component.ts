import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LineService } from '../../services/line.service';
import { ScheduleService } from '../../services/schedule.service';
import { Line, Schedule } from '../../models/models';

@Component({
  selector: 'app-schedules-public',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './schedules-public.component.html'
})
export class SchedulesPublicComponent implements OnInit {
  lines: Line[] = [];
  schedules: Schedule[] = [];
  selectedLineId: number | null = null;

  constructor(
    private lineService: LineService,
    private scheduleService: ScheduleService
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(data => this.lines = data);
  }

  onLineSelect() {
    if (this.selectedLineId) {
      this.scheduleService.getByLine(this.selectedLineId)
        .subscribe(data => this.schedules = data);
    }
  }
}