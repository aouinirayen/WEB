import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { Reduction } from '../../../core/models/models';

@Component({
  selector: 'app-reduction',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Operator</span> <span>/</span> Discounts</div>
    <h1 class="page-title"><i class="fas fa-percent text-orange"></i> Discount Codes</h1>
    <p class="page-subtitle">{{ reductions.length }} discount(s) total</p>
  </div>
  <button class="btn btn-primary" (click)="openModal()"><i class="fas fa-plus"></i> New Discount</button>
</div>

<div class="filters-bar">
  <div class="filter-search-wrap">
    <i class="fas fa-search"></i>
    <input type="text" class="filter-input" placeholder="Search by code..." [(ngModel)]="search" style="width:220px" (ngModelChange)="resetPage()">
  </div>
  <select class="filter-select" [(ngModel)]="filterValid" (ngModelChange)="resetPage()">
    <option value="">All</option>
    <option value="valid">Valid only</option>
    <option value="expired">Expired only</option>
  </select>
</div>

<div class="card">
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr><th>Promo Code</th><th>Discount</th><th>Points Required</th><th>Expiration</th><th>Status</th><th>Created by</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let r of paginated">
          <td>
            <span style="font-family:monospace;font-weight:700;background:#e3f2fd;color:#1a73e8;padding:4px 10px;border-radius:6px;">{{ r.code }}</span>
          </td>
          <td><strong class="text-green">-{{ r.pourcentage }}%</strong></td>
          <td><span class="cell-tag cell-tag-purple"><i class="fas fa-star"></i> {{ r.pointsRequis }} pts</span></td>
          <td class="cell-date"><i class="fas fa-calendar text-blue"></i> {{ r.dateExpiration }}</td>
          <td>
            <span class="status-badge" [ngClass]="r.estValide ? 'status-active' : 'status-expired'">
              <span class="status-dot"></span>{{ r.estValide ? 'VALID' : 'EXPIRED' }}
            </span>
          </td>
          <td class="text-muted fs-sm">{{ r.createdByUsername || '—' }}</td>
          <td>
            <div class="cell-actions">
              <button class="btn-icon btn-icon-edit" (click)="openModal(r)"><i class="fas fa-edit"></i></button>
              <button class="btn-icon btn-icon-delete" (click)="delete(r)"><i class="fas fa-trash"></i></button>
            </div>
          </td>
        </tr>
        <tr *ngIf="paginated.length === 0">
          <td colspan="7"><div class="table-empty"><i class="fas fa-percent"></i><div class="fw-bold">No discounts found</div></div></td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- PAGINATION -->
  <div class="pagination-bar" *ngIf="totalPages > 1">
    <button class="btn btn-outline btn-sm" (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 1">
      <i class="fas fa-chevron-left"></i>
    </button>
    <span class="pagination-info">Page {{ currentPage }} / {{ totalPages }} &nbsp;({{ filtered.length }} results)</span>
    <button class="btn btn-outline btn-sm" (click)="goToPage(currentPage + 1)" [disabled]="currentPage === totalPages">
      <i class="fas fa-chevron-right"></i>
    </button>
  </div>
</div>

<!-- MODAL -->
<div class="custom-modal-overlay" *ngIf="showModal" (click)="closeModal()">
  <div class="custom-modal" (click)="$event.stopPropagation()">
    <div class="custom-modal-header">
      <div class="custom-modal-title"><i class="fas fa-percent text-orange"></i> {{ editing ? 'Edit Discount' : 'New Discount' }}</div>
      <button class="custom-modal-close" (click)="closeModal()"><i class="fas fa-times"></i></button>
    </div>
    <div class="custom-modal-body">
      <div class="alert alert-danger" *ngIf="formError">
        <i class="fas fa-times-circle"></i> {{ formError }}
      </div>
      <div class="form-grid form-grid-2">
        <div class="form-group">
          <label class="form-label">Promo Code <span class="required">*</span></label>
          <input type="text" class="form-control" [(ngModel)]="form.code" placeholder="e.g. SUMMER25"
                 style="text-transform:uppercase"
                 [class.error]="submitted && !form.code.trim()"/>
          <div class="form-error" *ngIf="submitted && !form.code.trim()">Code is required.</div>
        </div>
        <div class="form-group">
          <label class="form-label">Percentage (%) <span class="required">*</span></label>
          <input type="number" class="form-control" [(ngModel)]="form.pourcentage"
                 placeholder="20" min="1" max="100"
                 [class.error]="submitted && (form.pourcentage < 1 || form.pourcentage > 100)"/>
          <div class="form-error" *ngIf="submitted && (form.pourcentage < 1 || form.pourcentage > 100)">
            Percentage must be between 1 and 100.
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">Expiration Date <span class="required">*</span></label>
          <input type="date" class="form-control" [(ngModel)]="form.dateExpiration"
                 [min]="today"
                 [class.error]="submitted && !form.dateExpiration"/>
          <div class="form-error" *ngIf="submitted && !form.dateExpiration">Expiration date is required.</div>
        </div>
        <div class="form-group">
          <label class="form-label">Points Required <span class="required">*</span></label>
          <input type="number" class="form-control" [(ngModel)]="form.pointsRequis"
                 placeholder="100" min="0"
                 [class.error]="submitted && form.pointsRequis < 0"/>
          <div class="form-error" *ngIf="submitted && form.pointsRequis < 0">Points must be >= 0.</div>
        </div>
      </div>
    </div>
    <div class="custom-modal-footer">
      <button class="btn btn-outline" (click)="closeModal()" type="button">Cancel</button>
      <button class="btn btn-primary" (click)="save()" [disabled]="saving" type="button"
              (mousedown)="console.log('Button reduction mousedown')"
              style="pointer-events: auto !important; cursor: pointer !important;">
        <i class="fas fa-spinner fa-spin" *ngIf="saving"></i>
        <i class="fas fa-save" *ngIf="!saving"></i>
        {{ saving ? 'Saving...' : 'Save' }}
      </button>
    </div>
  </div>
</div>
  `
})
export class ReductionComponent implements OnInit {
  protected readonly console = console;
  reductions: Reduction[] = [];
  search = ''; filterValid = '';
  showModal = false; editing = false; saving = false;
  submitted = false; formError = '';
  form: Reduction = { code: '', pourcentage: 10, dateExpiration: '', pointsRequis: 50 };
  editingId?: number;
  today = new Date().toISOString().split('T')[0];

  // Pagination
  currentPage = 1;
  readonly pageSize = 8;

  constructor(private api: ApiService, private auth: AuthService, private notif: NotificationService) {}

  ngOnInit() { this.load(); }

  load() {
    const opId = this.auth.getUserId();
    if (opId == null) { this.notif.error('Invalid session: please log in again.'); return; }
    this.api.getReductionsByOperator(opId).subscribe(d => this.reductions = d);
  }

  get filtered(): Reduction[] {
    return this.reductions.filter(r =>
      (!this.search || r.code.toLowerCase().includes(this.search.toLowerCase())) &&
      (!this.filterValid || (this.filterValid === 'valid' ? r.estValide : !r.estValide))
    );
  }

  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize) || 1; }

  get paginated(): Reduction[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  goToPage(p: number) { if (p >= 1 && p <= this.totalPages) this.currentPage = p; }
  resetPage() { this.currentPage = 1; }

  openModal(r?: Reduction) {
    console.log('Opening reduction modal', r);
    this.editing = !!r; this.editingId = r?.id;
    this.submitted = false;
    this.saving = false;
    this.formError = '';
    this.form = r ? { ...r } : { code: '', pourcentage: 10, dateExpiration: '', pointsRequis: 50 };
    this.showModal = true;
  }

  closeModal() {
    console.log('Closing reduction modal');
    this.showModal = false;
    this.submitted = false;
    this.formError = '';
    this.saving = false;
  }

  save() {
    console.log('Attempting to save reduction', this.form);
    this.submitted = true;
    this.formError = '';

    if (!this.form.code?.trim()) { this.formError = 'Promo code is required.'; console.warn('Save aborted: missing code'); return; }
    if (this.form.pourcentage < 1 || this.form.pourcentage > 100) {
      this.formError = 'Percentage must be between 1 and 100.'; console.warn('Save aborted: invalid percentage'); return;
    }
    if (!this.form.dateExpiration) { this.formError = 'Expiration date is required.'; console.warn('Save aborted: missing expiration'); return; }
    if (this.form.pointsRequis < 0) { this.formError = 'Points required must be >= 0.'; console.warn('Save aborted: invalid points'); return; }

    const opId = this.auth.getUserId();
    if (opId == null) { this.notif.error('Invalid session.'); console.error('Save aborted: no opId'); return; }

    this.saving = true;
    console.log('Saving reduction with payload:', this.form, 'operatorId:', opId);
    const obs = this.editing
      ? this.api.updateReduction(this.editingId!, this.form)
      : this.api.createReduction(this.form, opId);

    obs.subscribe({
      next: (res) => {
        console.log('Save reduction success', res);
        this.notif.success(this.editing ? 'Discount updated.' : 'Discount created.');
        this.load(); this.closeModal(); this.saving = false;
      },
      error: (err) => {
        console.error('Save reduction error', err);
        this.formError = err.error?.message || err.error?.error || 'An error occurred while saving.';
        this.saving = false;
      }
    });
  }

  delete(r: Reduction) {
    if (!confirm(`Delete code "${r.code}"? This action cannot be undone.`)) return;
    this.api.deleteReduction(r.id!).subscribe({
      next: () => { this.notif.success('Discount deleted.'); this.load(); },
      error: () => this.notif.error('Unable to delete this discount.')
    });
  }
}
