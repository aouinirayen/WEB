import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { SparePartService, SparePart } from '../../../services/spare-part.service';

@Component({
  selector: 'app-spare-part-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './spare-part-form.component.html'
})
export class SparePartFormComponent implements OnInit {
  part: SparePart = {
    name: '', category: 'ENGINE', referenceCode: '',
    stockQuantity: 0, minStockThreshold: 5, unitCost: 0
  };
  categories = ['ENGINE','BRAKES','TIRES','ELECTRICAL','FILTERS',
                'TRANSMISSION','BODYWORK','HVAC','OTHER'];
  isEdit   = false;
  id!: number;
  errorMsg = '';
  private readonly referencePattern = /^SP-[A-Z0-9-]{2,36}$/;

  constructor(
    private service: SparePartService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.id = this.route.snapshot.params['id'];
    if (this.id) {
      this.isEdit = true;
      this.service.getById(this.id).subscribe(data => this.part = data);
    } else {
      this.part.referenceCode = 'SP-';
    }
  }

  normalizeReferenceCode(value: string): void {
    let cleaned = (value ?? '').toUpperCase().replace(/\s+/g, '').replace(/[^A-Z0-9-]/g, '');
    if (!cleaned) {
      this.part.referenceCode = 'SP-';
      return;
    }

    cleaned = cleaned.replace(/^SP-?/, '');
    this.part.referenceCode = `SP-${cleaned.replace(/^-+/, '')}`.slice(0, 40);
  }

  save() {
    this.errorMsg = '';
    if (!this.part.name?.trim() || !this.part.referenceCode?.trim()) return;
    if (!this.referencePattern.test(this.part.referenceCode)) return;
    if (!this.part.category) return;
    if ((this.part.stockQuantity ?? 0) < 0 || (this.part.minStockThreshold ?? 0) <= 0) return;
    if ((this.part.unitCost ?? 0) <= 0) return;

    const obs = this.isEdit
      ? this.service.update(this.id, this.part)
      : this.service.create(this.part);

    obs.subscribe({
      next: () => this.router.navigate(['/admin/spare-parts']),
      error: err => this.errorMsg = err.error?.error || err.error?.message || 'An error occurred.'
    });
  }
}
