import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LineService } from '../../services/line.service';
import { StopService } from '../../services/stop.service';
import { Line, Stop } from '../../models/models';

@Component({
  selector: 'app-lines-public',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './lines-public.component.html'
})
export class LinesPublicComponent implements OnInit {
  lines: Line[] = [];
  stops: { [lineId: number]: Stop[] } = {};
  expandedLine: number | null = null;

  constructor(
    private lineService: LineService,
    private stopService: StopService
  ) {}

  ngOnInit() {
    this.lineService.getAll().subscribe(data => this.lines = data);
  }

  toggleLine(lineId: number) {
    if (this.expandedLine === lineId) {
      this.expandedLine = null;
    } else {
      this.expandedLine = lineId;
      if (!this.stops[lineId]) {
        this.stopService.getByLine(lineId).subscribe(
          data => this.stops[lineId] = data
        );
      }
    }
  }
}