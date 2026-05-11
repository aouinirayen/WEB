import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ScheduleService } from '../../services/schedule.service';
import { LineService } from '../../services/line.service';
import { VehicleService } from '../../services/vehicle.service';
import { Schedule, Line, Vehicle } from '../../models/models';

@Component({
  selector: 'app-schedule-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './schedule-form.component.html'
})
export class ScheduleFormComponent implements OnInit {
  schedule: Schedule = {
    line: { id: 0, name: '', code: '', mode: 'BUS', status: 'ACTIVE' },
    vehicle: { id: 0, plateNumber: '', brand: '', capacity: 0,
               mileage: 0, type: 'BUS', status: 'ACTIVE', purchaseDate: '' },
    dayType: 'WEEKDAY',
    startTime: '',
    endTime: '',
    frequencyMinutes: 15
  };
  lines: Line[] = [];
  vehicles: Vehicle[] = [];
  isEdit = false;
  id!: number;
  selectedLineId!: number;
  selectedVehicleId!: number;

  constructor(
    private service: ScheduleService,
    private lineService: LineService,
    private vehicleService: VehicleService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(data => this.lines = data);
    this.vehicleService.getAll().subscribe(data => this.vehicles = data);
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => {
        this.schedule = data;
        this.selectedLineId = data.line.id!;
        this.selectedVehicleId = data.vehicle.id!;
      });
    }
  }

  onLineChange() {
    const l = this.lines.find(l => l.id == this.selectedLineId);
    if (l) this.schedule.line = l;
  }

  onVehicleChange() {
    const v = this.vehicles.find(v => v.id == this.selectedVehicleId);
    if (v) this.schedule.vehicle = v;
  }

  private isTimeOrderValid(): boolean {
    return !!this.schedule.startTime &&
      !!this.schedule.endTime &&
      this.schedule.endTime > this.schedule.startTime;
  }

  save() {
    if (!this.selectedLineId || !this.selectedVehicleId) return;
    if (!this.isTimeOrderValid()) return;
    if (this.schedule.frequencyMinutes <= 0) return;

    if (this.isEdit) {
      this.service.update(this.id, this.schedule)
        .subscribe(() => this.router.navigate(['/admin/schedules']));
    } else {
      this.service.create(this.schedule)
        .subscribe(() => this.router.navigate(['/admin/schedules']));
    }
  }
}