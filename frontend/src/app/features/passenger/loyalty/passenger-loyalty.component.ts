import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { LoyaltyAccountResponse, PointTransaction, PricingPlan } from '../../../core/models/models';

@Component({
  selector: 'app-passenger-loyalty',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Passenger</span> <span>/</span> My Loyalty</div>
    <h1 class="page-title"><i class="fas fa-star text-orange"></i> Loyalty Program</h1>
  </div>
</div>

<!-- LOYALTY CARD -->
<div class="card" *ngIf="loyalty">
  <div class="card-body">
    <div style="display:flex;align-items:center;gap:20px">
      <div style="width:72px;height:72px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:24px;font-weight:700;flex-shrink:0"
           [ngStyle]="tierStyle()">
        {{ loyalty.niveau[0] }}
      </div>
      <div style="flex:1">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
          <span style="font-size:1.4rem;font-weight:700;color:var(--brand-primary)">{{ loyalty.pointsCumules }} points</span>
          <span class="status-badge" [ngClass]="'status-' + loyalty.niveau.toLowerCase()">
            <span class="status-dot"></span>{{ loyalty.niveau }}
          </span>
        </div>
        <div class="loyalty-bar-bg">
          <div class="loyalty-bar-fill" [ngClass]="'fill-' + loyalty.niveau.toLowerCase()"
               [style.width.%]="progressPercent()"></div>
        </div>
        <div style="font-size:0.82rem;color:var(--text-muted);margin-top:6px">{{ loyalty.messageProgression }}</div>
      </div>
    </div>
  </div>
</div>

<div class="grid-2">

  <!-- ── Remarque 5 : Affordable Subscriptions (plans <= loyalty points) ── -->
  <div class="card">
    <div class="card-header">
      <div class="card-title"><i class="fas fa-wallet text-green"></i> Subscriptions Payable with Points</div>
    </div>
    <div class="card-body" *ngIf="loyalty">
      <div class="alert alert-info mb-4">
        <i class="fas fa-info-circle"></i>
        Conversion: <strong>10 points = 1 DT</strong>. Only plans you can afford with your current points are shown.
      </div>

      <!-- counter badge -->
      <div class="flex-between mb-4">
        <span class="text-muted fs-sm">Your balance: <strong class="text-orange">{{ loyalty.pointsCumules }} pts</strong></span>
        <span class="cell-tag cell-tag-green" *ngIf="affordablePlans.length > 0">
          {{ affordablePlans.length }} plan(s) available
        </span>
      </div>

      <div *ngIf="affordablePlans.length === 0" style="color:var(--text-hint);font-size:0.85rem;text-align:center;padding:30px">
        <i class="fas fa-coins" style="font-size:2.5rem;color:#ddd;display:block;margin-bottom:12px"></i>
        <div class="fw-bold" style="margin-bottom:4px">Not enough points yet</div>
        <div class="fs-sm">Keep subscribing to earn more points and unlock plans.</div>
      </div>

      <div *ngFor="let p of affordablePlans" class="affordable-plan-row">
        <div style="min-width:0">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:4px">
            <span class="cell-tag" [ngClass]="typeClass(p.type)">{{ p.type }}</span>
            <span class="fw-bold" style="color:var(--brand-primary)">{{ p.nom }}</span>
          </div>
          <div class="fs-sm text-muted">{{ p.prix | number:'1.2-2' }} DT / {{ p.dureeEnJours }} days</div>
        </div>
        <div style="display:flex;align-items:center;gap:10px;flex-shrink:0">
          <span class="cell-tag cell-tag-orange fs-sm">
            <i class="fas fa-star"></i> {{ requiredPoints(p) }} pts
          </span>
          <button class="btn btn-success btn-sm" (click)="goToPlanPurchase(p)">
            <i class="fas fa-shopping-cart"></i> Buy
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- TRANSACTION HISTORY -->
  <div class="card">
    <div class="card-header"><div class="card-title"><i class="fas fa-history text-blue"></i> Points History</div></div>
    <div class="table-wrapper">
      <table class="data-table">
        <thead><tr><th>Type</th><th>Points</th><th>Description</th><th>Date</th></tr></thead>
        <tbody>
          <tr *ngFor="let t of paginatedTx">
            <td>
              <span class="status-badge" [ngClass]="t.type === 'EARNED' ? 'status-active' : 'status-pending'">
                <span class="status-dot"></span>{{ t.type === 'EARNED' ? 'EARNED' : 'USED' }}
              </span>
            </td>
            <td>
              <span [ngClass]="t.type === 'EARNED' ? 'text-green fw-bold' : 'text-orange fw-bold'">
                {{ t.type === 'EARNED' ? '+' : '-' }}{{ t.points }} pts
              </span>
            </td>
            <td class="text-muted fs-sm">{{ t.description }}</td>
            <td class="cell-date fs-sm">{{ t.date | date:'dd/MM/yyyy HH:mm' }}</td>
          </tr>
          <tr *ngIf="paginatedTx.length === 0">
            <td colspan="4"><div class="table-empty"><i class="fas fa-history"></i><div>No transactions yet</div></div></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination transactions -->
    <div class="pagination-bar" *ngIf="totalTxPages > 1">
      <button class="btn btn-outline btn-sm" (click)="goToTxPage(txPage - 1)" [disabled]="txPage === 1">
        <i class="fas fa-chevron-left"></i>
      </button>
      <span class="pagination-info">Page {{ txPage }} / {{ totalTxPages }}</span>
      <button class="btn btn-outline btn-sm" (click)="goToTxPage(txPage + 1)" [disabled]="txPage === totalTxPages">
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>
  </div>
</div>
  `,
  styles: [`
    .affordable-plan-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid var(--border-light);
      gap: 12px;
    }
    .affordable-plan-row:last-child { border-bottom: none; }
  `]
})
export class PassengerLoyaltyComponent implements OnInit {
  loyalty: LoyaltyAccountResponse | null = null;
  allPlans: PricingPlan[] = [];
  transactions: PointTransaction[] = [];
  private readonly pointsPerDt = 10;

  // Pagination transactions
  txPage = 1;
  readonly txPageSize = 8;

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private notif: NotificationService,
    private router: Router
  ) {}

  ngOnInit() { this.load(); }

  load() {
    const uid = this.auth.getUserId()!;
    this.api.getMyLoyalty(uid).subscribe(la => {
      this.loyalty = la;
      // Load ALL plans — filter done in getter (remarque 5)
      this.api.getAllPlans().subscribe(p => this.allPlans = p);
      this.api.getTransactions(la.id).subscribe(t => this.transactions = t);
    });
  }

  // ── Remarque 5 : only show plans the passenger can afford with their points ──
  get affordablePlans(): PricingPlan[] {
    if (!this.loyalty) return [];
    return this.allPlans.filter(p => this.requiredPoints(p) <= this.loyalty!.pointsCumules);
  }

  // Pagination helpers
  get totalTxPages(): number { return Math.ceil(this.transactions.length / this.txPageSize) || 1; }
  get paginatedTx(): PointTransaction[] {
    const start = (this.txPage - 1) * this.txPageSize;
    return this.transactions.slice(start, start + this.txPageSize);
  }
  goToTxPage(p: number) { if (p >= 1 && p <= this.totalTxPages) this.txPage = p; }

  progressPercent(): number {
    if (!this.loyalty) return 0;
    const pts = this.loyalty.pointsCumules;
    if (this.loyalty.niveau === 'GOLD') return 100;
    if (this.loyalty.niveau === 'SILVER') return Math.min(((pts - 200) / 300) * 100, 100);
    return Math.min((pts / 200) * 100, 100);
  }

  tierStyle() {
    const styles: any = {
      BRONZE: { background: '#fff8e1', color: '#8d6e63' },
      SILVER: { background: '#f5f5f5', color: '#607d8b' },
      GOLD:   { background: '#fff9c4', color: '#f57f17' }
    };
    return styles[this.loyalty?.niveau || 'BRONZE'];
  }

  requiredPoints(p: PricingPlan): number {
    return Math.ceil((p.prix || 0) * this.pointsPerDt);
  }

  goToPlanPurchase(p: PricingPlan) {
    this.router.navigate(['/passenger/plans'], { queryParams: { planId: p.id, payMode: 'POINTS' } });
  }

  typeClass(type: string) {
    return { 'cell-tag-green': type === 'FREE', 'cell-tag-blue': type === 'BASIC', 'cell-tag-purple': type === 'PREMIUM' };
  }
}
