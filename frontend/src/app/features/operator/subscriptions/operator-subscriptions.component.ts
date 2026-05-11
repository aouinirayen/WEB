import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { SubscriptionResponse, TransportType } from '../../../core/models/models';

@Component({
  selector: 'app-operator-subscriptions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Operator</span> <span>/</span> Subscriptions</div>
    <h1 class="page-title"><i class="fas fa-id-card text-blue"></i> Subscriptions</h1>
    <p class="page-subtitle">{{ filtered.length }} subscription(s)</p>
  </div>
</div>

<div class="stats-grid" style="grid-template-columns:repeat(3,1fr)">
  <div class="stat-card stat-card-green">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('ACTIVE') }}</div><div class="stat-card-label">Active</div></div>
      <i class="fas fa-check-circle stat-card-icon"></i>
    </div>
  </div>
  <div class="stat-card stat-card-orange">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('EXPIRED') }}</div><div class="stat-card-label">Expired</div></div>
      <i class="fas fa-clock stat-card-icon"></i>
    </div>
  </div>
  <div class="stat-card stat-card-red">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('CANCELLED') }}</div><div class="stat-card-label">Cancelled</div></div>
      <i class="fas fa-times-circle stat-card-icon"></i>
    </div>
  </div>
</div>

<div class="filters-bar">
  <div class="filter-search-wrap">
    <i class="fas fa-search"></i>
    <input type="text" class="filter-input" placeholder="Search by passenger..." [(ngModel)]="search" style="width:220px" (ngModelChange)="resetPage()">
  </div>
  <select class="filter-select" [(ngModel)]="filterStatus" (ngModelChange)="resetPage()">
    <option value="">All statuses</option>
    <option value="ACTIVE">ACTIVE</option>
    <option value="EXPIRED">EXPIRED</option>
    <option value="CANCELLED">CANCELLED</option>
  </select>
  <select class="filter-select" [(ngModel)]="filterTransport" (ngModelChange)="resetPage()">
    <option value="">All transports</option>
    <option value="BUS">BUS</option>
    <option value="METRO">METRO</option>
    <option value="TRAIN">TRAIN</option>
    <option value="LOUAGE">LOUAGE</option>
    <option value="BATTAH">BATTAH</option>
  </select>
</div>

<div class="card">
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr><th>Passenger</th><th>Plan</th><th>Status</th><th>Start</th><th>End</th><th>Points Earned</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let s of paginated">
          <td>
            <div class="cell-entity">
              <div class="cell-avatar cell-avatar-blue"><i class="fas fa-user"></i></div>
              <div>
                <div class="cell-entity-name">{{ s.passengerName }}</div>
                <div class="fs-sm text-muted">{{ s.passengerUsername }}</div>
              </div>
            </div>
          </td>
          <td>
            <div>
              <span class="cell-tag" [ngClass]="planClass(s.pricingPlan.type)">{{ s.pricingPlan.type }}</span>
              <div class="fs-sm text-muted mt-4">{{ s.pricingPlan.nom }}</div>
              <div class="fs-sm text-muted mt-4" *ngIf="s.pricingPlan.transportType">
                <i class="fas" [ngClass]="transportIcon(s.pricingPlan.transportType)"></i>
                {{ s.pricingPlan.transportType }}
              </div>
            </div>
          </td>
          <td>
            <span class="status-badge" [ngClass]="statusClass(s.statut)">
              <span class="status-dot"></span>{{ s.statut }}
            </span>
          </td>
          <td class="cell-date fs-sm"><i class="fas fa-calendar text-blue"></i> {{ s.dateDebut }}</td>
          <td class="cell-date fs-sm"><i class="fas fa-calendar-check text-green"></i> {{ s.dateFin }}</td>
          <td><span class="cell-tag cell-tag-orange"><i class="fas fa-star"></i> {{ s.pointsGagnes }} pts</span></td>
        </tr>
        <tr *ngIf="paginated.length === 0">
          <td colspan="6"><div class="table-empty"><i class="fas fa-id-card"></i><div>No subscriptions found</div></div></td>
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
  `
})
export class OperatorSubscriptionsComponent implements OnInit {
  subscriptions: SubscriptionResponse[] = [];
  search = ''; filterStatus = ''; filterTransport = '';

  currentPage = 1;
  readonly pageSize = 8;

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    const uid = this.auth.getUserId();
    if (uid == null) return;
    this.api.getSubscriptionsByOperator(uid).subscribe(d => this.subscriptions = d);
  }

  get filtered(): SubscriptionResponse[] {
    return this.subscriptions.filter(s =>
      (!this.search || s.passengerName?.toLowerCase().includes(this.search.toLowerCase()) || s.passengerUsername?.toLowerCase().includes(this.search.toLowerCase())) &&
      (!this.filterStatus || s.statut === this.filterStatus) &&
      (!this.filterTransport || s.pricingPlan?.transportType === this.filterTransport)
    );
  }

  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize) || 1; }

  get paginated(): SubscriptionResponse[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filtered.slice(start, start + this.pageSize);
  }

  goToPage(p: number) { if (p >= 1 && p <= this.totalPages) this.currentPage = p; }
  resetPage() { this.currentPage = 1; }

  count(status: string) { return this.subscriptions.filter(s => s.statut === status).length; }
  planClass(type?: string) { return { 'cell-tag-green': type === 'FREE', 'cell-tag-blue': type === 'BASIC', 'cell-tag-purple': type === 'PREMIUM' }; }
  statusClass(s: string) { return { 'status-active': s === 'ACTIVE', 'status-expired': s === 'EXPIRED', 'status-cancelled': s === 'CANCELLED' }; }
  transportIcon(t?: TransportType | string) {
    const val = (t as TransportType) || undefined;
    return { 'fa-bus': val === TransportType.BUS, 'fa-subway': val === TransportType.METRO, 'fa-train': val === TransportType.TRAIN, 'fa-tram': val === TransportType.LOUAGE, 'fa-ship': val === TransportType.BATTAH };
  }
}
