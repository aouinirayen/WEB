import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { LineService } from '../../services/line.service';
import { Line } from '../../models/models';

@Component({
  selector: 'app-line-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './line-list.component.html'
})
export class LineListComponent implements OnInit {
  lines: Line[] = [];

  constructor(private service: LineService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => this.lines = data);
  }

  delete(id: number) {
    if (confirm('Delete this line?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}