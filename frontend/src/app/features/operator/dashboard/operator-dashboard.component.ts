import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { ChurnPredictionResponse } from '../../../core/models/models';

@Component({
  selector: 'app-operator-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styleUrls: ['../../feature-styles.css'],
  styles: [`
    .ml-insights-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:16px; margin-bottom:24px; }
    .ml-insight-card {
      border-radius:12px; padding:20px 24px; display:flex; align-items:center; gap:16px;
      box-shadow:0 2px 12px rgba(0,0,0,0.08); background:#fff;
    }
    .ml-insight-red   { border-left:4px solid #c62828; }
    .ml-insight-green { border-left:4px solid #2e7d32; }
    .ml-insight-blue  { border-left:4px solid #1a73e8; }
    .ml-insight-icon {
      width:48px; height:48px; border-radius:10px;
      display:flex; align-items:center; justify-content:center; font-size:20px; flex-shrink:0;
    }
    .ml-insight-red   .ml-insight-icon { background:#fce4ec; color:#c62828; }
    .ml-insight-green .ml-insight-icon { background:#e8f5e9; color:#2e7d32; }
    .ml-insight-blue  .ml-insight-icon { background:#e3f2fd; color:#1a73e8; }
    .ml-insight-body { flex:1; }
    .ml-insight-value { font-size:1.8rem; font-weight:800; line-height:1; margin-bottom:4px; }
    .ml-insight-red   .ml-insight-value { color:#c62828; }
    .ml-insight-green .ml-insight-value { color:#2e7d32; }
    .ml-insight-blue  .ml-insight-value { color:#1a73e8; }
    .ml-insight-label { font-size:0.82rem; font-weight:600; color:#555; }
    .ml-insight-sub   { font-size:0.74rem; color:#999; margin-top:2px; }
    .ml-loading-badge { display:inline-flex; align-items:center; gap:6px; font-size:0.78rem; color:#999; }
  `],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Home</span> <span>/</span> Dashboard</div>
    <h1 class="page-title"><i class="fas fa-tachometer-alt text-blue"></i> Operator Dashboard</h1>
    <p class="page-subtitle">Overview of your modules</p>
  </div>
</div>

<!-- STATS -->
<div class="stats-grid">
  <div class="stat-card stat-card-blue">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ stats.plans }}</div><div class="stat-card-label">Pricing Plans</div></div>
      <i class="fas fa-tags stat-card-icon"></i>
    </div>
    <div class="stat-card-footer"><i class="fas fa-arrow-right"></i> <a routerLink="/operator/pricing-plans" style="color:inherit">Manage plans</a></div>
  </div>
  <div class="stat-card stat-card-green">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ stats.activeSubscriptions }}</div><div class="stat-card-label">Active Subscriptions</div></div>
      <i class="fas fa-id-card stat-card-icon"></i>
    </div>
    <div class="stat-card-footer"><i class="fas fa-arrow-right"></i> <a routerLink="/operator/subscriptions" style="color:inherit">View all</a></div>
  </div>
  <div class="stat-card stat-card-orange">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ stats.reductions }}</div><div class="stat-card-label">Active Discounts</div></div>
      <i class="fas fa-percent stat-card-icon"></i>
    </div>
    <div class="stat-card-footer"><i class="fas fa-arrow-right"></i> <a routerLink="/operator/reductions" style="color:inherit">Manage</a></div>
  </div>
  <div class="stat-card stat-card-purple">
    <div class="stat-card-header">
      <div><div class="stat-card-value">{{ stats.loyaltyMembers }}</div><div class="stat-card-label">Loyalty Members</div></div>
      <i class="fas fa-star stat-card-icon"></i>
    </div>
    <div class="stat-card-footer"><i class="fas fa-arrow-right"></i> <a routerLink="/operator/loyalty" style="color:inherit">View</a></div>
  </div>
</div>

<!-- ML INSIGHTS -->
<div style="margin-bottom:8px;display:flex;align-items:center;justify-content:space-between">
  <div style="font-size:0.9rem;font-weight:700;color:var(--brand-primary)">
    <i class="fas fa-brain text-purple"></i>&nbsp; ML Insights — your passengers
  </div>
  <div *ngIf="mlLoading" class="ml-loading-badge">
    <i class="fas fa-spinner fa-spin"></i> Analyzing...
  </div>
  <a *ngIf="!mlLoading" routerLink="/operator/ml" class="btn btn-outline btn-sm">
    Full analysis <i class="fas fa-arrow-right"></i>
  </a>
</div>

<div class="ml-insights-grid">
  <div class="ml-insight-card ml-insight-red">
    <div class="ml-insight-icon"><i class="fas fa-exclamation-triangle"></i></div>
    <div class="ml-insight-body">
      <div class="ml-insight-value">
        <span *ngIf="!mlLoading">{{ mlKpis.highRisk }} / {{ mlKpis.total }}</span>
        <span *ngIf="mlLoading" style="font-size:1.2rem">—</span>
      </div>
      <div class="ml-insight-label">High-risk passengers</div>
      <div class="ml-insight-sub">Churn probability ≥ 70%</div>
    </div>
  </div>

  <div class="ml-insight-card ml-insight-green">
    <div class="ml-insight-icon"><i class="fas fa-coins"></i></div>
    <div class="ml-insight-body">
      <div class="ml-insight-value">
        <span *ngIf="!mlLoading">{{ mlKpis.avgCLV | number:'1.0-0' }} DT</span>
        <span *ngIf="mlLoading" style="font-size:1.2rem">—</span>
      </div>
      <div class="ml-insight-label">Avg. Customer Lifetime Value</div>
      <div class="ml-insight-sub">Estimated over 12 months</div>
    </div>
  </div>

  <div class="ml-insight-card ml-insight-blue">
    <div class="ml-insight-icon"><i class="fas fa-arrow-circle-up"></i></div>
    <div class="ml-insight-body">
      <div class="ml-insight-value">
        <span *ngIf="!mlLoading">{{ mlKpis.upgradeReady }} / {{ mlKpis.total }}</span>
        <span *ngIf="mlLoading" style="font-size:1.2rem">—</span>
      </div>
      <div class="ml-insight-label">Upgrade-ready passengers</div>
      <div class="ml-insight-sub">Recommended PREMIUM plan</div>
    </div>
  </div>
</div>

<div class="grid-3">
  <div class="card">
    <div class="card-header"><div class="card-title"><i class="fas fa-bolt text-orange"></i> Quick Actions</div></div>
    <div class="card-body">
      <div class="quick-actions">
        <a routerLink="/operator/pricing-plans" class="quick-action-btn qa-blue"><i class="fas fa-plus"></i> New Pricing Plan</a>
        <a routerLink="/operator/reductions"    class="quick-action-btn qa-green"><i class="fas fa-plus"></i> New Discount</a>
        <a routerLink="/operator/ml"            class="quick-action-btn qa-purple"><i class="fas fa-brain"></i> ML Analysis</a>
        <a routerLink="/operator/loyalty"       class="quick-action-btn qa-orange"><i class="fas fa-star"></i> Loyalty Program</a>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header"><div class="card-title"><i class="fas fa-star text-orange"></i> Loyalty Distribution</div></div>
    <div class="card-body">
      <div style="margin-bottom:14px">
        <div class="flex-between mb-4"><span>GOLD</span><span class="fw-bold text-orange">{{ stats.goldMembers }}</span></div>
        <div class="progress-bar-wrap"><div class="progress-bar progress-bar-orange" [style.width]="getPercent(stats.goldMembers) + '%'"></div></div>
      </div>
      <div style="margin-bottom:14px">
        <div class="flex-between mb-4"><span>SILVER</span><span class="fw-bold text-muted">{{ stats.silverMembers }}</span></div>
        <div class="progress-bar-wrap"><div class="progress-bar" style="background:linear-gradient(90deg,#90a4ae,#607d8b)" [style.width]="getPercent(stats.silverMembers) + '%'"></div></div>
      </div>
      <div>
        <div class="flex-between mb-4"><span>BRONZE</span><span class="fw-bold" style="color:#8d6e63">{{ stats.bronzeMembers }}</span></div>
        <div class="progress-bar-wrap"><div class="progress-bar" style="background:linear-gradient(90deg,#a1887f,#8d6e63)" [style.width]="getPercent(stats.bronzeMembers) + '%'"></div></div>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header"><div class="card-title"><i class="fas fa-exclamation-triangle text-red"></i> Churn Risks</div></div>
    <div class="card-body">
      <div *ngIf="highRisk.length === 0" style="color:var(--text-hint);font-size:0.85rem">
        <i class="fas fa-check-circle text-green"></i> No high-risk passengers detected
      </div>
      <div *ngFor="let c of highRisk.slice(0,4)" class="recent-item" style="padding:8px 0;">
        <div>
          <div class="recent-item-main">{{ c.username }}</div>
          <div class="recent-item-sub">{{ c.suggestedAction }}</div>
        </div>
        <span class="status-badge status-expired">{{ (c.churnProbability * 100).toFixed(0) }}%</span>
      </div>
      <div class="mt-4" *ngIf="highRisk.length > 0">
        <a routerLink="/operator/ml" class="btn btn-outline btn-sm">View full analysis</a>
      </div>
    </div>
  </div>
</div>
  `
})
export class OperatorDashboardComponent implements OnInit {

  stats = { plans:0, activeSubscriptions:0, reductions:0, loyaltyMembers:0, goldMembers:0, silverMembers:0, bronzeMembers:0 };
  highRisk: ChurnPredictionResponse[] = [];
  mlLoading = true;
  mlKpis    = { highRisk:0, avgCLV:0, upgradeReady:0, total:0 };

  constructor(private api: ApiService, private auth: AuthService) {}

  ngOnInit() {
    const opId = this.auth.getUserId();
    if (opId == null) return;

    this.api.getPlansByOperator(opId).subscribe(d => this.stats.plans = d.length);
    this.api.getSubscriptionsByOperator(opId).subscribe(d => {
      this.stats.activeSubscriptions = d.filter(s => s.statut === 'ACTIVE').length;
      const unique = new Set(d.map(s => s.passengerUsername).filter((u): u is string => !!u));
      this.stats.loyaltyMembers = unique.size;
    });
    this.api.getReductionsByOperator(opId).subscribe(d => {
      const today = new Date();
      this.stats.reductions = d.filter(r => new Date(r.dateExpiration) >= today).length;
    });

    forkJoin({ loyalty: this.api.getAllLoyalty(), subs: this.api.getSubscriptionsByOperator(opId) })
      .subscribe(({ loyalty, subs }) => {
        const usernames = new Set(subs.map(s => s.passengerUsername).filter((u): u is string => !!u));
        const accounts  = loyalty.filter(a => usernames.has(a.passengerUsername));
        this.stats.goldMembers   = accounts.filter(a => a.niveau === 'GOLD').length;
        this.stats.silverMembers = accounts.filter(a => a.niveau === 'SILVER').length;
        this.stats.bronzeMembers = accounts.filter(a => a.niveau === 'BRONZE').length;
        if (this.stats.loyaltyMembers === 0 && accounts.length > 0) this.stats.loyaltyMembers = accounts.length;
      });

    forkJoin({ churnList: this.api.predictChurnAll(), subs: this.api.getSubscriptionsByOperator(opId) })
      .subscribe(({ churnList, subs }) => {
        const usernames        = new Set(subs.map(s => s.passengerUsername).filter((u): u is string => !!u));
        const myChurn          = churnList.filter(c => usernames.has(c.username));
        this.highRisk          = myChurn.filter(c => c.riskLevel === 'HIGH');
        this.mlKpis.highRisk   = this.highRisk.length;
        this.mlKpis.total      = myChurn.length;
        this.mlKpis.upgradeReady = myChurn.filter(c =>
          c.suggestedAction?.toLowerCase().includes('premium') ||
          c.suggestedAction?.toLowerCase().includes('upgrade')
        ).length;

        const passengerIds = [...new Set(
          subs.filter(s => usernames.has(s.passengerUsername ?? ''))
              .map(s => s.passengerId).filter((id): id is number => id != null)
        )];

        if (passengerIds.length > 0) {
          forkJoin(passengerIds.map(id => this.api.predictCLV(id))).subscribe({
            next: (results) => {
              const vals         = results.map(r => r.clvValue).filter(v => v > 0);
              this.mlKpis.avgCLV = vals.length > 0 ? vals.reduce((a, b) => a + b, 0) / vals.length : 0;
              this.mlLoading     = false;
            },
            error: () => { this.mlLoading = false; }
          });
        } else {
          this.mlLoading = false;
        }
      });
  }

  getPercent(val: number): number {
    return this.stats.loyaltyMembers > 0 ? Math.round((val / this.stats.loyaltyMembers) * 100) : 0;
  }
}
