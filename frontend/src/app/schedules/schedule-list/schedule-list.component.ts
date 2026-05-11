import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ScheduleService } from '../../services/schedule.service';
import { Schedule } from '../../models/models';

@Component({
  selector: 'app-schedule-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './schedule-list.component.html'
})
export class ScheduleListComponent implements OnInit {
  schedules: Schedule[] = [];

  constructor(private service: ScheduleService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.schedules = data);
  }

  delete(id: number) {
    if (confirm('Delete this schedule?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}