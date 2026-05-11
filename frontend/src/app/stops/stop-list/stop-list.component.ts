import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { StopService } from '../../services/stop.service';
import { Stop } from '../../models/models';

@Component({
  selector: 'app-stop-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './stop-list.component.html'
})
export class StopListComponent implements OnInit {
  stops: Stop[] = [];

  constructor(private service: StopService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.stops = data);
  }

  delete(id: number) {
    if (confirm('Delete this stop?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}