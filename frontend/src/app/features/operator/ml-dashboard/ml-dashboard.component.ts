import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin, map } from 'rxjs';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import {
  PlanRecommendationResponse,
  ChurnPredictionResponse,
  CLVResponse,
  ActionSendResponse
} from '../../../core/models/models';

@Component({
  selector: 'app-ml-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Operator</span> <span>/</span> AI & ML</div>
    <h1 class="page-title"><i class="fas fa-brain text-purple"></i> AI & ML Analysis</h1>
    <p class="page-subtitle">Data restricted to passengers subscribed to at least one of your plans</p>
  </div>
  <button class="btn btn-primary" (click)="analyzeAll()" [disabled]="loadingAll || myPassengers.length === 0">
    <i class="fas fa-spinner fa-spin" *ngIf="loadingAll"></i>
    <i class="fas fa-play" *ngIf="!loadingAll"></i>
    {{ loadingAll ? 'Analyzing...' : 'Analyze all' }}
  </button>
</div>

<div *ngIf="myPassengers.length === 0" class="alert alert-warning mb-6">
  <i class="fas fa-user-slash"></i>
  No passengers found for your subscriptions (or <code>passengerId</code> missing from the API response).
</div>

<!-- ROW 1 : Recommendation + Churn -->
<div class="grid-2 mb-6">

  <!-- 1. Plan Recommendation -->
  <div class="card">
    <div class="card-header">
      <div class="card-title"><i class="fas fa-lightbulb text-orange"></i> Plan Recommendation</div>
    </div>
    <div class="card-body">
      <div class="form-group mb-4">
        <label class="form-label">Passenger</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <select class="form-control" style="flex:1;min-width:160px" [(ngModel)]="recUserId" [disabled]="myPassengers.length === 0">
            <option *ngFor="let p of myPassengers" [ngValue]="p.id">{{ p.label }}</option>
          </select>
          <button class="btn btn-primary" (click)="loadRecommendation()" [disabled]="recUserId == null">Analyze</button>
        </div>
      </div>
      <div *ngIf="recommendation" style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px">
        <div class="flex-between mb-4">
          <span class="text-muted fs-sm">Recommended plan</span>
          <span class="cell-tag" [ngClass]="planTypeClass(recommendation.recommendedPlan)">{{ recommendation.recommendedPlan }}</span>
        </div>
        <div class="mb-4">
          <div class="flex-between mb-4">
            <span class="text-muted fs-sm">Confidence</span>
            <span class="fw-bold">{{ recommendation.confidence }}%</span>
          </div>
          <div class="progress-bar-wrap">
            <div class="progress-bar progress-bar-blue" [style.width]="recommendation.confidence + '%'"></div>
          </div>
        </div>
        <div style="font-size:0.82rem;color:var(--text-muted);font-style:italic">{{ recommendation.reason }}</div>
      </div>
    </div>
  </div>

  <!-- 2. Churn Prediction -->
  <div class="card">
    <div class="card-header">
      <div class="card-title"><i class="fas fa-exclamation-triangle text-red"></i> Churn Prediction</div>
    </div>
    <div class="card-body">
      <div class="form-group mb-4">
        <label class="form-label">Passenger</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <select class="form-control" style="flex:1;min-width:160px" [(ngModel)]="churnUserId" [disabled]="myPassengers.length === 0">
            <option *ngFor="let p of myPassengers" [ngValue]="p.id">{{ p.label }}</option>
          </select>
          <button class="btn btn-primary" (click)="loadChurn()" [disabled]="churnUserId == null">Predict</button>
        </div>
      </div>
      <div *ngIf="churnPrediction" style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px">
        <div class="flex-between mb-4">
          <span class="text-muted fs-sm">Risk level</span>
          <span class="status-badge" [ngClass]="riskClass(churnPrediction.riskLevel)">
            <span class="status-dot"></span>{{ churnPrediction.riskLevel }}
          </span>
        </div>
        <div class="mb-4">
          <div style="font-size:2rem;font-weight:700;margin-bottom:8px;" [ngClass]="churnColor(churnPrediction.churnProbability)">
            {{ (churnPrediction.churnProbability * 100).toFixed(1) }}%
          </div>
          <div class="progress-bar-wrap">
            <div class="progress-bar" [style.width]="(churnPrediction.churnProbability * 100) + '%'" [style.background]="churnBg(churnPrediction.churnProbability)"></div>
          </div>
        </div>
        <div style="font-size:0.82rem;color:var(--text-muted)">{{ churnPrediction.suggestedAction }}</div>
        <div *ngIf="churnPrediction.suggestedPromoCode" style="margin-top:8px">
          <span style="font-size:0.78rem;color:var(--text-muted)">Suggested coupon: </span>
          <span style="font-family:monospace;font-weight:700;background:#e3f2fd;color:#1a73e8;padding:3px 8px;border-radius:4px;">{{ churnPrediction.suggestedPromoCode }}</span>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- ROW 2 : CLV + Recommended Action -->
<div class="grid-2 mb-6">

  <!-- 3. Customer Lifetime Value -->
  <div class="card">
    <div class="card-header">
      <div class="card-title"><i class="fas fa-coins text-green"></i> Customer Lifetime Value</div>
    </div>
    <div class="card-body">
      <div class="form-group mb-4">
        <label class="form-label">Passenger</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <select class="form-control" style="flex:1;min-width:160px" [(ngModel)]="clvUserId" [disabled]="myPassengers.length === 0">
            <option *ngFor="let p of myPassengers" [ngValue]="p.id">{{ p.label }}</option>
          </select>
          <button class="btn btn-primary" (click)="loadCLV()" [disabled]="clvUserId == null">
            <i class="fas fa-spinner fa-spin" *ngIf="clvLoading"></i>
            Calculate
          </button>
        </div>
      </div>
      <div *ngIf="clvResult" style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px">
        <div class="flex-between mb-4">
          <span class="text-muted fs-sm">Estimated value (12 months)</span>
          <strong style="font-size:1.6rem;color:#2e7d32">{{ clvResult.clvValue | number:'1.2-2' }} DT</strong>
        </div>
        <div class="mb-4">
          <div class="progress-bar-wrap">
            <div class="progress-bar" style="background:linear-gradient(90deg,#2e7d32,#66bb6a)" [style.width]="clvPercent(clvResult.clvValue) + '%'"></div>
          </div>
          <div style="display:flex;justify-content:space-between;font-size:0.72rem;color:var(--text-muted);margin-top:4px">
            <span>0 DT</span><span>300+ DT</span>
          </div>
        </div>
        <div style="font-size:0.82rem;color:var(--text-muted);font-style:italic">{{ clvResult.interpretation }}</div>
      </div>
    </div>
  </div>

  <!-- 4. Recommended Action — FIX: card wrapper added -->
  <div class="card">
    <div class="card-header">
      <div class="card-title"><i class="fas fa-bolt text-orange"></i> Recommended Action</div>
    </div>
    <div class="card-body">
      <div class="form-group mb-4">
        <label class="form-label">Passenger</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <select class="form-control" style="flex:1;min-width:160px" [(ngModel)]="actionUserId" [disabled]="myPassengers.length === 0">
            <option *ngFor="let p of myPassengers" [ngValue]="p.id">{{ p.label }}</option>
          </select>
          <button class="btn btn-primary" (click)="loadAction()" [disabled]="actionUserId == null">
            <i class="fas fa-spinner fa-spin" *ngIf="actionLoading"></i>
            Get Action
          </button>
        </div>
      </div>

      <div *ngIf="actionResult" style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px">
        <div class="flex-between mb-4">
          <span class="text-muted fs-sm">Suggested action</span>
            <span style="font-family:monospace;font-size:0.78rem;font-weight:700;background:#fff3e0;color:#e65100;padding:4px 10px;border-radius:8px">
            {{ actionResult.suggestedPromoCode || actionResult.suggestedAction }}
          </span>
        </div>
        <div style="display:flex;align-items:flex-start;gap:10px;padding:12px;background:#fff8e1;border-radius:8px;border-left:3px solid #f57f17;margin-bottom:14px">
          <i class="fas fa-info-circle" style="color:#f57f17;margin-top:2px"></i>
          <span style="font-size:0.85rem;color:#555">{{ actionResult.suggestedAction }}</span>
        </div>
        <button class="btn btn-success" style="width:100%;justify-content:center" (click)="sendAction()" [disabled]="sendingAction">
          <i class="fas fa-spinner fa-spin" *ngIf="sendingAction"></i>
          <i class="fas fa-paper-plane" *ngIf="!sendingAction"></i>
          {{ sendingAction ? 'Sending...' : 'Send promo code by email' }}
        </button>
      </div>

      <!-- Success confirmation after send -->
      <div *ngIf="sentActionResult" style="background:#e8f5e9;border-radius:var(--radius-sm);padding:16px;margin-top:12px;border:1px solid #a5d6a7">
        <div class="flex-between mb-4">
          <span style="font-weight:600;color:#2e7d32"><i class="fas fa-check-circle"></i> Email sent!</span>
          <span class="cell-tag cell-tag-green">{{ sentActionResult.discountPercentage }}% off</span>
        </div>
        <div class="flex-between mb-2">
          <span class="text-muted fs-sm">Generated code</span>
          <span style="font-family:monospace;font-weight:700;font-size:1.1rem;background:#e3f2fd;color:#1a237e;padding:4px 12px;border-radius:8px;letter-spacing:2px">
            {{ sentActionResult.promoCode }}
          </span>
        </div>
        <div style="font-size:0.78rem;color:#666;margin-top:8px">
          <i class="fas fa-clock"></i> Valid for 7 days — 0 points required
        </div>
      </div>
    </div>
  </div>
</div>

<!-- GLOBAL ANALYSIS TABLE -->
<div class="card" *ngIf="allAnalysis.length > 0">
  <div class="card-header">
    <div class="card-title"><i class="fas fa-table text-blue"></i> Global Analysis — your passengers</div>
  </div>
  <div class="table-wrapper">
    <table class="data-table">
      <thead>
        <tr>
          <th>Username</th><th>Recommended plan</th><th>Confidence</th>
          <th>Churn risk</th><th>Probability</th><th>CLV (12 mo.)</th><th>Action</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let a of allAnalysis">
          <td class="fw-bold">{{ a.username }}</td>
          <td><span class="cell-tag" [ngClass]="planTypeClass(a.recommendedPlan)">{{ a.recommendedPlan }}</span></td>
          <td>
            <div style="display:flex;align-items:center;gap:8px">
              <div style="flex:1;background:#e0e0e0;border-radius:10px;height:6px;overflow:hidden;min-width:60px">
                <div style="height:6px;border-radius:10px;background:#1a73e8" [style.width]="a.confidence + '%'"></div>
              </div>
              <span class="fs-sm">{{ a.confidence }}%</span>
            </div>
          </td>
          <td>
            <span class="status-badge" [ngClass]="riskClass(a.riskLevel)">
              <span class="status-dot"></span>{{ a.riskLevel }}
            </span>
          </td>
          <td [ngClass]="churnColor(a.churnProbability)" class="fw-bold">{{ (a.churnProbability * 100).toFixed(1) }}%</td>
          <td>
            <span *ngIf="a.clvValue != null" [ngClass]="clvColor(a.clvValue)" class="fw-bold">{{ a.clvValue | number:'1.2-2' }} DT</span>
            <span *ngIf="a.clvValue == null" class="text-muted fs-sm">—</span>
          </td>
          <td class="fs-sm text-muted">{{ a.suggestedAction }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
  `
})
export class MlDashboardComponent implements OnInit {

  myPassengers: { id: number; label: string }[] = [];

  recUserId:    number | null = null;
  churnUserId:  number | null = null;
  clvUserId:    number | null = null;
  actionUserId: number | null = null;

  recommendation:  PlanRecommendationResponse | null = null;
  churnPrediction: ChurnPredictionResponse    | null = null;
  clvResult:       CLVResponse                | null = null;
  actionResult:    ChurnPredictionResponse    | null = null;

  clvLoading    = false;
  actionLoading = false;
  sendingAction = false;
  sentActionResult: ActionSendResponse | null = null;

  allAnalysis: (ChurnPredictionResponse & { recommendedPlan: string; confidence: number; clvValue: number | null; })[] = [];
  loadingAll = false;

  constructor(private api: ApiService, private auth: AuthService, private notif: NotificationService) {}

  ngOnInit() {
    const opId = this.auth.getUserId();
    if (opId == null) { this.notif.error('Invalid session: please log in again.'); return; }
    this.api.getSubscriptionsByOperator(opId).subscribe({
      next: (subs) => {
        const byId = new Map<number, string>();
        for (const s of subs) {
          if (s.passengerId != null && !byId.has(s.passengerId))
            byId.set(s.passengerId, s.passengerName || s.passengerUsername || `#${s.passengerId}`);
        }
        this.myPassengers = [...byId.entries()].map(([id, label]) => ({ id, label }));
        if (this.myPassengers.length > 0) {
          this.recUserId = this.churnUserId = this.clvUserId = this.actionUserId = this.myPassengers[0].id;
        }
      },
      error: () => this.notif.error('Unable to load your subscriptions.'),
    });
  }

  loadRecommendation() {
    if (this.recUserId == null) { this.notif.error('Please select a passenger.'); return; }
    this.api.recommendPlan(this.recUserId).subscribe({
      next: (r) => (this.recommendation = r),
      error: () => this.notif.error('Recommendation failed.'),
    });
  }

  loadChurn() {
    if (this.churnUserId == null) { this.notif.error('Please select a passenger.'); return; }
    this.api.predictChurn(this.churnUserId).subscribe({
      next: (r) => (this.churnPrediction = r),
      error: () => this.notif.error('Churn prediction failed.'),
    });
  }

  loadCLV() {
    if (this.clvUserId == null) { this.notif.error('Please select a passenger.'); return; }
    this.clvLoading = true;
    this.api.predictCLV(this.clvUserId).subscribe({
      next: (r) => { this.clvResult = r; this.clvLoading = false; },
      error: () => { this.notif.error('CLV prediction failed.'); this.clvLoading = false; },
    });
  }

  loadAction() {
    if (this.actionUserId == null) { this.notif.error('Please select a passenger.'); return; }
    this.actionLoading    = true;
    this.sentActionResult = null;
    this.api.predictChurn(this.actionUserId).subscribe({
      next: (r) => { this.actionResult = r; this.actionLoading = false; },
      error: () => { this.notif.error('Action prediction failed.'); this.actionLoading = false; },
    });
  }

  sendAction() {
    if (this.actionUserId == null) return;
    this.sendingAction    = true;
    this.sentActionResult = null;
    this.api.sendAction(this.actionUserId).subscribe({
      next: (r) => {
        this.sentActionResult = r;
        this.sendingAction    = false;
        this.notif.success(`Code ${r.promoCode} sent to ${r.username}!`);
      },
      error: () => { this.sendingAction = false; this.notif.error('Failed to send action email.'); },
    });
  }

  analyzeAll() {
    const opId = this.auth.getUserId();
    if (opId == null) { this.notif.error('Invalid session.'); return; }
    this.loadingAll = true; this.allAnalysis = [];
    forkJoin({ subs: this.api.getSubscriptionsByOperator(opId), churnList: this.api.predictChurnAll() }).subscribe({
      next: ({ subs, churnList }) => {
        const usernames = new Set(subs.map(s => s.passengerUsername).filter((u): u is string => !!u && u.length > 0));
        const filtered  = churnList.filter(c => usernames.has(c.username));
        if (filtered.length === 0) { this.loadingAll = false; this.notif.info('No passengers from your scope found.'); return; }
        forkJoin(
          filtered.map(c =>
            forkJoin({ rec: this.api.recommendPlan(c.userId), clv: this.api.predictCLV(c.userId) }).pipe(
              map(({ rec, clv }) => ({ ...c, recommendedPlan: rec.recommendedPlan, confidence: rec.confidence, clvValue: clv?.clvValue ?? null }))
            )
          )
        ).subscribe({
          next: (rows) => { this.allAnalysis = [...rows].sort((a, b) => b.churnProbability - a.churnProbability); this.loadingAll = false; },
          error: () => { this.loadingAll = false; this.notif.error('Global analysis failed.'); },
        });
      },
      error: () => { this.loadingAll = false; this.notif.error('Unable to load ML data.'); },
    });
  }

  planTypeClass(type: string) { return { 'cell-tag-green': type === 'FREE', 'cell-tag-blue': type === 'BASIC', 'cell-tag-purple': type === 'PREMIUM' }; }
  riskClass(level: string)    { return { 'status-expired': level === 'HIGH', 'status-pending': level === 'MODERATE', 'status-active': level === 'LOW' }; }
  churnColor(p: number)       { return { 'text-red': p >= 0.7, 'text-orange': p >= 0.4 && p < 0.7, 'text-green': p < 0.4 }; }
  churnBg(p: number)          { return p >= 0.7 ? '#c62828' : p >= 0.4 ? '#f57f17' : '#2e7d32'; }
  clvPercent(v: number)       { return Math.min((v / 300) * 100, 100); }
  clvColor(v: number)         { return { 'text-green': v >= 150, 'text-orange': v >= 50 && v < 150, 'text-muted': v < 50 }; }
}
