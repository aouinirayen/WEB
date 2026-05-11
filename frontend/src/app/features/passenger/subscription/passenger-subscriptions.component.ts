import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { SubscriptionResponse } from '../../../core/models/models';

@Component({
  selector: 'app-passenger-subscriptions',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Passenger</span> <span>/</span> My Subscriptions</div>
    <h1 class="page-title"><i class="fas fa-id-card text-blue"></i> My Subscriptions</h1>
  </div>
  <a routerLink="/passenger/plans" class="btn btn-primary">
    <i class="fas fa-plus"></i> New Subscription
  </a>
</div>

<div *ngIf="activeSubscriptions.length > 0" class="alert alert-success">
  <i class="fas fa-check-circle"></i>
  You have <strong>{{ activeSubscriptions.length }}</strong> active subscription(s).
</div>

<div *ngIf="activeSubscriptions.length === 0 && subscriptions.length === 0" class="card">
  <div class="table-empty" style="padding:60px">
    <i class="fas fa-id-card"></i>
    <div class="fw-bold" style="margin-bottom:8px">No subscriptions yet</div>
    <div class="fs-sm" style="margin-bottom:16px">Subscribe to a plan to start travelling</div>
    <a routerLink="/passenger/plans" class="btn btn-primary"><i class="fas fa-bus"></i> Browse Plans</a>
  </div>
</div>

<div class="card" *ngIf="subscriptions.length > 0">
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr><th>Plan</th><th>Status</th><th>Start</th><th>End</th><th>Auto-renewal</th><th>Points Earned</th><th>Actions</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let s of paginated">
          <td>
            <div class="cell-entity">
              <div class="cell-avatar" [ngClass]="avatarClass(s.pricingPlan.type)">
                <i class="fas fa-tag"></i>
              </div>
              <div>
                <div class="cell-entity-name">{{ s.pricingPlan.nom }}</div>
                <div class="fs-sm text-muted">{{ s.pricingPlan.prix | number:'1.2-2' }} DT / {{ s.pricingPlan.dureeEnJours }} days</div>
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
          <td>
            <button *ngIf="s.statut === 'ACTIVE'" type="button" class="btn btn-sm"
              [ngClass]="s.autoRenewal ? 'btn-primary' : 'btn-outline'"
              (click)="toggleAutoRenew(s)" [disabled]="renewBusyId === s.id">
              <i class="fas fa-sync" *ngIf="renewBusyId !== s.id"></i>
              <i class="fas fa-spinner fa-spin" *ngIf="renewBusyId === s.id"></i>
              {{ s.autoRenewal ? 'On' : 'Off' }}
            </button>
            <span *ngIf="s.statut !== 'ACTIVE'" class="cell-muted">—</span>
          </td>
          <td>
            <span class="cell-tag cell-tag-orange">
              <i class="fas fa-star"></i> +{{ s.pointsGagnes }} pts
            </span>
          </td>
          <td>
            <button *ngIf="s.statut === 'ACTIVE'" class="btn btn-danger btn-sm" (click)="cancel(s)">
              <i class="fas fa-times"></i> Cancel
            </button>
            <span *ngIf="s.statut !== 'ACTIVE'" class="cell-muted">—</span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- PAGINATION -->
  <div class="pagination-bar" *ngIf="totalPages > 1">
    <button class="btn btn-outline btn-sm" (click)="goToPage(currentPage - 1)" [disabled]="currentPage === 1">
      <i class="fas fa-chevron-left"></i>
    </button>
    <span class="pagination-info">Page {{ currentPage }} / {{ totalPages }} &nbsp;({{ subscriptions.length }} total)</span>
    <button class="btn btn-outline btn-sm" (click)="goToPage(currentPage + 1)" [disabled]="currentPage === totalPages">
      <i class="fas fa-chevron-right"></i>
    </button>
  </div>
</div>
  `
})
export class PassengerSubscriptionsComponent implements OnInit {
  subscriptions: SubscriptionResponse[] = [];
  activeSubscriptions: SubscriptionResponse[] = [];
  renewBusyId: number | null = null;

  currentPage = 1;
  readonly pageSize = 8;

  constructor(private api: ApiService, private auth: AuthService, private notif: NotificationService) {}

  ngOnInit() { this.load(); }

  load() {
    const uid = this.auth.getUserId()!;
    this.api.getMySubscriptions(uid).subscribe(d => {
    this.subscriptions = d.sort((a, b) => {
    // ACTIVE always first
    const statusOrder: Record<string, number> = { ACTIVE: 0, EXPIRED: 1, CANCELLED: 2 };
    const sa = statusOrder[a.statut] ?? 99;
    const sb = statusOrder[b.statut] ?? 99;
    if (sa !== sb) return sa - sb;
    // Within same status → most recent first
    return new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime();
    });      this.activeSubscriptions = d.filter(s => s.statut === 'ACTIVE');
    });
  }

  get totalPages(): number { return Math.ceil(this.subscriptions.length / this.pageSize) || 1; }
  get paginated(): SubscriptionResponse[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.subscriptions.slice(start, start + this.pageSize);
  }
  goToPage(p: number) { if (p >= 1 && p <= this.totalPages) this.currentPage = p; }

  cancel(s: SubscriptionResponse) {
    if (!confirm('Cancel this subscription?')) return;
    this.api.cancelSubscription(s.id, this.auth.getUserId()!).subscribe({
      next: () => { this.notif.success('Subscription cancelled.'); this.load(); },
      error: () => this.notif.error('Unable to cancel this subscription.')
    });
  }

  toggleAutoRenew(s: SubscriptionResponse) {
    const uid = this.auth.getUserId();
    if (uid == null) return;
    const next = !s.autoRenewal;
    this.renewBusyId = s.id;
    this.api.updateSubscriptionAutoRenewal(s.id, uid, next).subscribe({
      next: (res) => {
        s.autoRenewal = !!res.autoRenewal;
        this.renewBusyId = null;
        this.notif.success(next ? 'Auto-renewal enabled.' : 'Auto-renewal disabled.');
      },
      error: () => {
        this.renewBusyId = null;
        this.notif.error('Unable to update auto-renewal.');
      }
    });
  }

  avatarClass(type?: string) { return { 'cell-avatar-green': type === 'FREE', 'cell-avatar-blue': type === 'BASIC', 'cell-avatar-purple': type === 'PREMIUM' }; }
  statusClass(s: string) { return { 'status-active': s === 'ACTIVE', 'status-expired': s === 'EXPIRED', 'status-cancelled': s === 'CANCELLED' }; }
}
