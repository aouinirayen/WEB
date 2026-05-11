import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { StopService } from '../../services/stop.service';
import { LineService } from '../../services/line.service';
import { Stop, Line } from '../../models/models';

@Component({
  selector: 'app-stop-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './stop-form.component.html'
})
export class StopFormComponent implements OnInit {
  stop: Stop = {
    name: '', sequence: 1, latitude: 0,
    longitude: 0, line: { id: 0, name: '', code: '', mode: 'BUS', status: 'ACTIVE' }
  };
  lines: Line[] = [];
  isEdit = false;
  id!: number;
  selectedLineId!: number;

  constructor(
    private service: StopService,
    private lineService: LineService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(data => this.lines = data);
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => {
        this.stop = data;
        this.selectedLineId = data.line.id!;
      });
    }
  }

  onLineChange() {
    const l = this.lines.find(l => l.id == this.selectedLineId);
    if (l) this.stop.line = l;
  }

  save() {
    if (!this.stop.name?.trim() || !this.selectedLineId) return;
    if (this.stop.sequence <= 0) return;
    if (this.stop.latitude < -90 || this.stop.latitude > 90) return;
    if (this.stop.longitude < -180 || this.stop.longitude > 180) return;

    if (this.isEdit) {
      this.service.update(this.id, this.stop)
        .subscribe(() => this.router.navigate(['/admin/stops']));
    } else {
      this.service.create(this.stop)
        .subscribe(() => this.router.navigate(['/admin/stops']));
    }
  }
}