import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { LoyaltyAccountResponse } from '../../../core/models/models';

@Component({
  selector: 'app-operator-loyalty',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Operator</span> <span>/</span> Loyalty</div>
    <h1 class="page-title"><i class="fas fa-star text-orange"></i> Loyalty Program</h1>
    <p class="page-subtitle">{{ accounts.length }} loyalty account(s) — passengers subscribed to your plans</p>
  </div>
</div>

<div class="stats-grid" style="grid-template-columns:repeat(4,1fr)">
  <div class="stat-card stat-card-purple">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ accounts.length }}</div><div class="stat-card-label">Total Members</div></div>
      <i class="fas fa-users stat-card-icon"></i>
    </div>
  </div>
  <div class="stat-card" style="background:linear-gradient(135deg,#a1887f,#8d6e63)">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('BRONZE') }}</div><div class="stat-card-label">Bronze</div></div>
      <i class="fas fa-star stat-card-icon"></i>
    </div>
  </div>
  <div class="stat-card" style="background:linear-gradient(135deg,#90a4ae,#607d8b)">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('SILVER') }}</div><div class="stat-card-label">Silver</div></div>
      <i class="fas fa-star stat-card-icon"></i>
    </div>
  </div>
  <div class="stat-card stat-card-orange">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ count('GOLD') }}</div><div class="stat-card-label">Gold</div></div>
      <i class="fas fa-star stat-card-icon"></i>
    </div>
  </div>
</div>

<div class="filters-bar">
  <div class="filter-search-wrap">
    <i class="fas fa-search"></i>
    <input type="text" class="filter-input" placeholder="Search..." [(ngModel)]="search" style="width:220px">
  </div>
  <select class="filter-select" [(ngModel)]="filterTier">
    <option value="">All tiers</option>
    <option value="BRONZE">BRONZE</option>
    <option value="SILVER">SILVER</option>
    <option value="GOLD">GOLD</option>
  </select>
</div>

<div class="card">
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr><th>Passenger</th><th>Tier</th><th>Points</th><th>Progress</th><th>Next milestone</th></tr>
      </thead>
      <tbody>
        <tr *ngFor="let la of filtered">
          <td>
            <div class="cell-entity">
              <div class="cell-avatar" [ngStyle]="avatarStyle(la.niveau)">
                {{ la.passengerName ? la.passengerName[0] : '?' }}
              </div>
              <div>
                <div class="cell-entity-name">{{ la.passengerName }}</div>
                <div class="fs-sm text-muted">{{ la.passengerUsername }}</div>
              </div>
            </div>
          </td>
          <td>
            <span class="status-badge" [ngClass]="'status-' + la.niveau.toLowerCase()">
              <span class="status-dot"></span>{{ la.niveau }}
            </span>
          </td>
          <td><strong class="text-orange">{{ la.pointsCumules }} pts</strong></td>
          <td style="min-width:160px">
            <div class="loyalty-bar-bg">
              <div class="loyalty-bar-fill" [ngClass]="'fill-' + la.niveau.toLowerCase()"
                   [style.width.%]="progressPct(la)"></div>
            </div>
          </td>
          <td class="fs-sm text-muted">{{ la.messageProgression }}</td>
        </tr>
        <tr *ngIf="filtered.length === 0">
          <td colspan="5"><div class="table-empty"><i class="fas fa-star"></i><div>No loyalty accounts found for your subscriptions</div></div></td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
  `
})
export class OperatorLoyaltyComponent implements OnInit {
  accounts: LoyaltyAccountResponse[] = [];
  search = ''; filterTier = '';

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private notif: NotificationService,
  ) {}

  ngOnInit() {
    const opId = this.auth.getUserId();
    if (opId == null) {
      this.notif.error('Invalid session: please log in again.');
      return;
    }
    forkJoin({
      loyalty: this.api.getAllLoyalty(),
      subs: this.api.getSubscriptionsByOperator(opId),
    }).subscribe({
      next: ({ loyalty, subs }) => {
        const usernames = new Set(
          subs.map((s) => s.passengerUsername).filter((u): u is string => !!u && u.length > 0)
        );
        this.accounts = loyalty.filter((a) => usernames.has(a.passengerUsername));
      },
      error: () => this.notif.error('Unable to load loyalty data.'),
    });
  }

  get filtered(): LoyaltyAccountResponse[] {
    return this.accounts.filter(a =>
      (!this.search || a.passengerName?.toLowerCase().includes(this.search.toLowerCase())) &&
      (!this.filterTier || a.niveau === this.filterTier)
    );
  }

  count(tier: string) { return this.accounts.filter(a => a.niveau === tier).length; }

  progressPct(la: LoyaltyAccountResponse): number {
    const pts = la.pointsCumules;
    if (la.niveau === 'GOLD') return 100;
    if (la.niveau === 'SILVER') return Math.min(((pts - 200) / 300) * 100, 100);
    return Math.min((pts / 200) * 100, 100);
  }

  avatarStyle(niveau: string) {
    const styles: Record<string, Record<string, string>> = {
      BRONZE: { background: '#fff8e1', color: '#8d6e63', border: '2px solid #a1887f' },
      SILVER: { background: '#f5f5f5', color: '#607d8b', border: '2px solid #90a4ae' },
      GOLD:   { background: '#fff9c4', color: '#f57f17', border: '2px solid #ffd54f' }
    };
    return { ...styles[niveau], width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: '700', flexShrink: '0' };
  }
}