import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { LineService } from '../../services/line.service';
import { Line } from '../../models/models';

@Component({
  selector: 'app-line-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './line-form.component.html'
})
export class LineFormComponent implements OnInit {
    line: Line = {
    name: '',
    code: '',
    mode: 'BUS',
    status: 'ACTIVE'
  };
  isEdit = false;
  id!: number;

  constructor(
    private service: LineService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => this.line = data);
    }
  }

  save() {
    if (!this.line.name?.trim() || !this.line.code?.trim()) return;

    if (this.isEdit) {
      this.service.update(this.id, this.line)
        .subscribe(() => this.router.navigate(['/admin/lines']));
    } else {
      this.service.create(this.line)
        .subscribe(() => this.router.navigate(['/admin/lines']));
    }
  }
}