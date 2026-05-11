/***import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { IncidentService } from '../../../services/incident.service';
import { IncidentReport } from '../../../models/models';

@Component({
  selector: 'app-incident-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './incident-list.component.html'
})
export class IncidentListComponent implements OnInit {
  incidents: IncidentReport[] = [];

  constructor(private service: IncidentService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.incidents = data);
  }

  delete(id: number) {
    if (confirm('Delete this incident report?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}*/
