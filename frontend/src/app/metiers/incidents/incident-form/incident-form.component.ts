/**import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { IncidentService } from '../../services/incident.service';
import { VehicleService } from './../../../services/vehicle.service';
import { LineService } from './../../../services/line.service';
import { IncidentReport, Vehicle, Line } from '../../../models/models';

@Component({
  selector: 'app-incident-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './incident-form.component.html'
})
export class IncidentFormComponent implements OnInit {
  incident: IncidentReport = {
    vehicle: {} as Vehicle,
    type: 'BREAKDOWN',
    severity: 'MEDIUM',
    status: 'OPEN',
    description: '',
    reportedAt: '',
    reportedBy: ''
  };
  vehicles: Vehicle[] = [];
  lines: Line[] = [];
  isEdit = false;
  id!: number;

  constructor(
    private service: IncidentService,
    private vehicleService: VehicleService,
    private lineService: LineService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.vehicleService.getAll().subscribe(data => this.vehicles = data);
    this.lineService.getAll().subscribe(data => this.lines = data);
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => this.incident = data);
    }
  }

  compareById(a: any, b: any): boolean {
    return a && b && a.id === b.id;
  }

  save() {
    if (this.isEdit) {
      this.service.update(this.id, this.incident)
        .subscribe(() => this.router.navigate(['/incidents']));
    } else {
      this.service.create(this.incident)
        .subscribe(() => this.router.navigate(['/incidents']));
    }
  }***/
