import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { LoyaltyAccountResponse, PricingPlan, TransportType, PlanRecommendationResponse } from '../../../core/models/models';

@Component({
  selector: 'app-passenger-plans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  styleUrls: ['../../feature-styles.css'],
  styles: [`
    .passenger-transport-btns { gap: 12px; align-items: stretch; }
    .passenger-transport-btns .btn { min-height: 44px; padding: 10px 18px; font-size: 0.9rem; }
  `],
  template: `
<div class="page-header">
  <div>
    <div class="breadcrumb"><span>Passenger</span> <span>/</span> Available Plans</div>
    <h1 class="page-title"><i class="fas fa-bus text-blue"></i> Choose your subscription</h1>
    <p class="page-subtitle">{{ filtered.length }} plan(s) displayed{{ transportFilterHint }}</p>
  </div>
</div>

<!-- AI RECOMMENDATION -->
<div class="alert alert-info" *ngIf="recommendation">
  <i class="fas fa-brain"></i>
  Our AI recommends the <strong>{{ recommendation.recommendedPlan }}</strong> plan
  ({{ recommendation.confidence }}% confidence) — {{ recommendation.reason }}
</div>

<div class="alert alert-warning" *ngIf="hasActiveSubscription">
  <i class="fas fa-exclamation-triangle"></i>
  You already have an active subscription. You can purchase another plan without cancelling the current one.
</div>

<!-- Transport filter buttons -->
<div class="mb-6">
  <div class="text-muted fs-sm mb-4"><i class="fas fa-route"></i> Transport type</div>
  <div class="filters-bar passenger-transport-btns">
    <button type="button" class="btn" [ngClass]="filterTransport === '' ? 'btn-primary' : 'btn-outline'" (click)="setTransport('')">
      <i class="fas fa-th"></i> All
    </button>
    <button type="button" *ngFor="let t of allTransportButtons" class="btn"
      [ngClass]="filterTransport === t.value ? 'btn-primary' : 'btn-outline'"
      (click)="setTransport(t.value)">
      <i class="fas" [ngClass]="t.icon"></i> {{ t.label }}
    </button>
  </div>
</div>

<div class="filters-bar">
  <select class="filter-select" [(ngModel)]="filterType">
    <option value="">All types</option>
    <option value="FREE">FREE</option>
    <option value="BASIC">BASIC</option>
    <option value="PREMIUM">PREMIUM</option>
  </select>
  <select class="filter-select" [(ngModel)]="sortBy">
    <option value="prix-asc">Price: low to high</option>
    <option value="prix-desc">Price: high to low</option>
    <option value="duree">Duration</option>
  </select>
</div>

<!-- PLANS GRID -->
<div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(300px,1fr));gap:20px;margin-bottom:24px">
  <div *ngFor="let p of filtered" class="card" style="cursor:pointer;transition:transform 0.2s,box-shadow 0.2s"
       [style.border]="recommendation?.recommendedPlan === p.type ? '2px solid #1a73e8' : ''"
       (mouseenter)="hovered=p.id" (mouseleave)="hovered=null">

    <div *ngIf="recommendation?.recommendedPlan === p.type"
         style="background:#1a73e8;color:#fff;text-align:center;padding:6px;font-size:0.78rem;font-weight:700">
      <i class="fas fa-brain"></i> Recommended by our AI
    </div>

    <div class="card-body">
      <div class="flex-between mb-4">
        <div style="display:flex;align-items:center;gap:10px">
          <div class="cell-avatar" [ngClass]="avatarClass(p.type)"><i class="fas fa-tag"></i></div>
          <span class="cell-tag" [ngClass]="typeClass(p.type)">{{ p.type }}</span>
        </div>
        <span *ngIf="p.transportType" class="cell-tag cell-tag-teal">
          <i class="fas" [ngClass]="transportIcon(p.transportType)"></i> {{ p.transportType }}
        </span>
      </div>

      <h3 style="font-size:1.1rem;font-weight:700;color:var(--brand-primary);margin-bottom:6px">{{ p.nom }}</h3>
      <p style="font-size:0.82rem;color:var(--text-muted);margin-bottom:16px">{{ p.description }}</p>

      <div style="margin-bottom:16px">
        <span style="font-size:2rem;font-weight:700;color:var(--brand-primary)">{{ p.prix | number:'1.2-2' }}</span>
        <span style="font-size:0.88rem;color:var(--text-muted)"> DT / {{ p.dureeEnJours }} days</span>
      </div>

      <div style="font-size:0.78rem;color:var(--text-hint);margin-bottom:16px">
        <i class="fas fa-coins" style="color:#f57f17"></i>
        +{{ p.prix | number:'1.0-0' }} loyalty points on purchase
      </div>

      <button class="btn btn-primary" style="width:100%;justify-content:center" (click)="subscribe(p)">
        <i class="fas fa-shopping-cart"></i> Subscribe
      </button>
    </div>
  </div>
</div>

<div *ngIf="filtered.length === 0" class="card">
  <div class="table-empty">
    <i class="fas fa-bus"></i>
    <div class="fw-bold">{{ plans.length === 0 ? 'No plans available' : 'No plans match these criteria' }}</div>
    <div class="fs-sm">{{ plans.length === 0 ? 'Check back later or contact an operator.' : 'Try changing the transport, plan type or sort order.' }}</div>
  </div>
</div>

<!-- SUBSCRIPTION MODAL -->
<div class="custom-modal-overlay" *ngIf="showModal" (click)="closeModal()">
  <div class="custom-modal" (click)="$event.stopPropagation()">
    <div class="custom-modal-header">
      <div class="custom-modal-title"><i class="fas fa-shopping-cart text-blue"></i> Confirm subscription</div>
      <button class="custom-modal-close" (click)="closeModal()"><i class="fas fa-times"></i></button>
    </div>
    <div class="custom-modal-body" *ngIf="selectedPlan">
      <div style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px;margin-bottom:16px">
        <div class="flex-between mb-4">
          <span class="text-muted">Selected plan</span>
          <span class="fw-bold">{{ selectedPlan.nom }}</span>
        </div>
        <div class="flex-between mb-4">
          <span class="text-muted">Duration</span>
          <span>{{ selectedPlan.dureeEnJours }} days</span>
        </div>
        <div class="flex-between mb-4">
          <span class="text-muted">Discount applied</span>
          <span class="text-green" *ngIf="discount > 0">-{{ discount | number:'1.2-2' }} DT</span>
          <span class="text-muted" *ngIf="discount === 0">—</span>
        </div>
        <div class="flex-between" style="border-top:1px solid var(--border-mid);padding-top:10px;margin-top:10px">
          <span class="fw-bold">Total</span>
          <span style="font-size:1.4rem;font-weight:700;color:var(--brand-primary)">{{ finalPrice | number:'1.2-2' }} DT</span>
        </div>
      </div>

      <div class="form-group mb-4">
        <label class="form-label">Payment method</label>
        <div style="display:flex;gap:8px;flex-wrap:wrap">
          <button type="button" class="btn" [ngClass]="payMode === 'CASH' ? 'btn-primary' : 'btn-outline'" (click)="setPayMode('CASH')">
            <i class="fas fa-money-bill-wave"></i> Cash
          </button>
          <button type="button" class="btn" [ngClass]="payMode === 'POINTS' ? 'btn-primary' : 'btn-outline'" (click)="setPayMode('POINTS')">
            <i class="fas fa-star"></i> Loyalty points
          </button>
        </div>
        <div class="fs-sm text-muted mt-4">Conversion: 10 points = 1 DT</div>
        <div class="fs-sm mt-4" *ngIf="payMode === 'POINTS'">
          <div class="flex-between">
            <span class="text-muted">Available points</span>
            <strong>{{ loyaltyPoints }} pts</strong>
          </div>
          <div class="flex-between">
            <span class="text-muted">Required points</span>
            <strong>{{ requiredPoints }} pts</strong>
          </div>
          <div class="alert alert-warning mt-4" *ngIf="!canPayWithPoints">
            <i class="fas fa-exclamation-triangle"></i> Not enough points for this plan.
          </div>
        </div>
      </div>

      <div class="form-group mb-4">
        <label class="flex-between" style="cursor:pointer;align-items:flex-start;gap:12px">
          <input type="checkbox" [(ngModel)]="autoRenewal"/>
          <span>
            <span class="fw-bold">Auto-renewal</span>
            <span class="fs-sm text-muted" style="display:block;margin-top:4px">
              Email reminders 7 days and 1 day before expiry; renewal charged via Stripe.
            </span>
          </span>
        </label>
      </div>

      <div class="form-group mb-4">
        <label class="form-label">Promo code (optional)</label>
        <div style="display:flex;gap:8px">
          <input type="text" class="form-control" [(ngModel)]="promoCode"
                 placeholder="e.g. SUMMER20" style="text-transform:uppercase"/>
          <button class="btn btn-outline" (click)="applyPromo()">Apply</button>
        </div>
        <div class="alert alert-success mt-4" *ngIf="promoMsg && promoValid">
          <i class="fas fa-check"></i> {{ promoMsg }}
        </div>
        <div class="alert alert-danger mt-4" *ngIf="promoMsg && !promoValid">
          <i class="fas fa-times"></i> {{ promoMsg }}
        </div>
      </div>

      <div class="stripe-badge" *ngIf="payMode === 'CASH'">
        <i class="fas fa-lock"></i> Secured payment via Stripe
      </div>
    </div>
    <div class="custom-modal-footer">
      <button class="btn btn-outline" (click)="closeModal()">Cancel</button>
      <button class="btn btn-success" (click)="confirmPayment()" [disabled]="paying">
        <i class="fas fa-spinner fa-spin" *ngIf="paying"></i>
        <i class="fas fa-credit-card" *ngIf="!paying"></i>
        {{ paying ? 'Redirecting...' : payLabel }}
      </button>
    </div>
  </div>
</div>
  `
})
export class PassengerPlansComponent implements OnInit {
  plans: PricingPlan[] = [];
  recommendation: PlanRecommendationResponse | null = null;
  hasActiveSubscription = false;
  filterTransport = ''; filterType = ''; sortBy = 'prix-asc';
  showModal = false; paying = false;
  selectedPlan: PricingPlan | null = null;
  promoCode = ''; promoMsg = ''; promoValid = false;
  discount = 0; finalPrice = 0;
  hovered: number | null | undefined = null;
  autoRenewal = false;
  loyaltyPoints = 0;
  payMode: 'CASH' | 'POINTS' = 'CASH';
  requiredPoints = 0;
  canPayWithPoints = false;
  private readonly pointsPerDt = 10;

  /** All transport modes — always visible as buttons for the passenger. */
  readonly allTransportButtons: { value: string; label: string; icon: string }[] = [
    { value: 'BUS',    label: 'Bus',    icon: 'fa-bus'    },
    { value: 'METRO',  label: 'Metro',  icon: 'fa-subway' },
    { value: 'TRAIN',  label: 'Train',  icon: 'fa-train'  },
    { value: 'LOUAGE', label: 'Louage', icon: 'fa-taxi'   },
    { value: 'BATTAH', label: 'Battah', icon: 'fa-ship'   },
  ];

  constructor(
    private api: ApiService,
    private auth: AuthService,
    private notif: NotificationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.api.getAllPlans().subscribe((d) => {
      this.plans = d.map((p) => ({
        ...p,
        transportType: p.transportType
          ? (String(p.transportType).toUpperCase() as TransportType)
          : p.transportType,
      }));
      const qpPlanId = Number(this.route.snapshot.queryParamMap.get('planId'));
      const qpMode   = (this.route.snapshot.queryParamMap.get('payMode') || '').toUpperCase();
      if (qpPlanId) {
        const preselected = this.plans.find((p) => p.id === qpPlanId);
        if (preselected) {
          this.subscribe(preselected);
          if (qpMode === 'POINTS') this.setPayMode('POINTS');
        }
      }
    });
    const userId = this.auth.getUserId();
    if (userId) {
      this.api.recommendPlan(userId).subscribe({
        next: (r) => { this.recommendation = r; },
        error: () => { this.recommendation = null; }
      });
      this.api.getMySubscriptions(userId).subscribe({
        next: (subs) => { this.hasActiveSubscription = subs.some((s) => s.statut === 'ACTIVE'); },
        error: () => { this.hasActiveSubscription = false; }
      });
      this.api.getMyLoyalty(userId).subscribe({
        next: (la: LoyaltyAccountResponse) => { this.loyaltyPoints = la.pointsCumules || 0; this.refreshPointsState(); },
        error: () => { this.loyaltyPoints = 0; this.refreshPointsState(); }
      });
    }
  }

  get transportFilterHint(): string {
    if (!this.filterTransport) return '';
    const opt = this.allTransportButtons.find((o) => o.value === this.filterTransport);
    return opt ? ` — ${opt.label}` : '';
  }

  setTransport(t: string) { this.filterTransport = t; }

  get filtered(): PricingPlan[] {
    const ft = this.filterTransport ? String(this.filterTransport).toUpperCase() : '';
    let list = this.plans.filter(p => {
      const pt = p.transportType ? String(p.transportType).toUpperCase() : '';
      return (!ft || pt === ft) && (!this.filterType || p.type === this.filterType);
    });
    if (this.sortBy === 'prix-asc')  list = list.sort((a, b) => a.prix - b.prix);
    else if (this.sortBy === 'prix-desc') list = list.sort((a, b) => b.prix - a.prix);
    else if (this.sortBy === 'duree')     list = list.sort((a, b) => a.dureeEnJours - b.dureeEnJours);
    return list;
  }

  subscribe(p: PricingPlan) {
    this.selectedPlan = p;
    this.discount = 0; this.finalPrice = p.prix;
    this.promoCode = ''; this.promoMsg = ''; this.promoValid = false;
    this.autoRenewal = false; this.payMode = 'CASH';
    this.refreshPointsState();
    this.showModal = true;
  }

  setPayMode(mode: 'CASH' | 'POINTS') { this.payMode = mode; this.refreshPointsState(); }

  get payLabel(): string {
    return this.payMode === 'POINTS'
      ? `Pay ${this.requiredPoints} pts`
      : `Pay ${this.finalPrice.toFixed(2)} DT`;
  }

  private isReductionExpired(r: { dateExpiration?: string | null; estValide?: boolean | null }): boolean {
    if (r.estValide === false) return true;
    const raw = (r.dateExpiration ?? '').trim();
    if (!raw) return true;
    const asDateOnly = /^\d{4}-\d{2}-\d{2}$/.test(raw);
    const exp = asDateOnly ? new Date(`${raw}T23:59:59.999`) : new Date(raw);
    if (Number.isNaN(exp.getTime())) return true;
    return exp.getTime() < Date.now();
  }

  private clearPromo(reasonMsg?: string) {
    if (!this.selectedPlan) return;
    this.discount = 0; this.finalPrice = this.selectedPlan.prix;
    this.promoValid = false; this.promoMsg = reasonMsg ?? '';
    this.refreshPointsState();
  }

  private refreshPointsState() {
    const basePrice = this.finalPrice || this.selectedPlan?.prix || 0;
    this.requiredPoints   = Math.ceil(basePrice * this.pointsPerDt);
    this.canPayWithPoints = this.loyaltyPoints >= this.requiredPoints;
  }

  applyPromo() {
    if (!this.promoCode.trim()) return;
    this.api.getReductionByCode(this.promoCode.toUpperCase()).subscribe({
      next: (r) => {
        if (!this.selectedPlan || this.isReductionExpired(r)) {
          this.clearPromo('Code expired or inactive.'); return;
        }
        const dis = this.selectedPlan.prix * r.pourcentage / 100;
        this.discount  = dis;
        this.finalPrice = this.selectedPlan.prix - dis;
        this.promoMsg   = `Valid code! -${r.pourcentage}% applied.`;
        this.promoValid = true;
        this.refreshPointsState();
      },
      error: () => { this.clearPromo('Invalid or expired code.'); }
    });
  }

  confirmPayment() {
    const passengerId  = this.auth.getUserId();
    const pricingPlanId = this.selectedPlan?.id;
    if (passengerId == null || pricingPlanId == null) {
      this.notif.error('Invalid session or missing plan ID: please log in again.');
      return;
    }
    if (this.payMode === 'POINTS' && !this.canPayWithPoints) {
      this.notif.error('Not enough points for this plan.');
      return;
    }

    const code = this.promoValid && this.promoCode?.trim()
      ? this.promoCode.trim().toUpperCase() : undefined;

    this.paying = true;

    const proceed = (validatedCode?: string) => {
      const fallbackToLegacyPayment = () => {
        // Compatibility fallback for backends that do not support /payment/initiate/me request shape.
        const legacyPayload: any = { passengerId, pricingPlanId };
        if (validatedCode) legacyPayload.codeReduction = validatedCode;
        if (this.autoRenewal) legacyPayload.autoRenewal = true;
        if (this.payMode === 'POINTS') {
          legacyPayload.paymentMode = 'POINTS';
          legacyPayload.pointsToUse = this.requiredPoints;
        }
        this.api.initiatePayment(legacyPayload).subscribe({
          next: (legacyRes) => {
            if (!legacyRes?.checkoutUrl || String(legacyRes.checkoutUrl).trim() === '') {
              this.notif.error('Legacy payment init returned no checkout URL.');
              this.paying = false;
              return;
            }
            window.location.href = legacyRes.checkoutUrl;
          },
          error: (legacyErr) => {
            // Final fallback for backends configured without Stripe endpoints.
            if (this.payMode === 'CASH') {
              this.api.subscribe({
                pricingPlanId,
                ...(validatedCode ? { codeReduction: validatedCode } : {})
              }, passengerId)
                .subscribe({
                  next: () => {
                    this.notif.success('Subscription created successfully.');
                    this.closeModal();
                    this.paying = false;
                    this.router.navigate(['/passenger/subscriptions']);
                  },
                  error: (subErr) => {
                    const subMsg = subErr.error?.message
                      || (Array.isArray(subErr.error?.errors) ? subErr.error.errors.join(' ') : null)
                      || subErr.message
                      || 'Payment/subscription failed. Please contact support.';
                    this.notif.error(String(subMsg));
                    this.paying = false;
                  }
                });
              return;
            }

            const legacyMsg = legacyErr.error?.message
              || (Array.isArray(legacyErr.error?.errors) ? legacyErr.error.errors.join(' ') : null)
              || legacyErr.message
              || 'Error initiating Stripe payment.';
            this.notif.error(String(legacyMsg));
            this.paying = false;
          }
        });
      };

      this.api.initiatePaymentMe(
        pricingPlanId, validatedCode, this.autoRenewal,
        this.payMode, this.payMode === 'POINTS' ? this.requiredPoints : undefined
      ).subscribe({
        next: (res) => {
          if (this.payMode === 'POINTS') {
            if (res?.checkoutUrl && String(res.checkoutUrl).trim() !== '') {
              this.notif.error('Points payment not activated server-side (Stripe checkout returned).');
              this.paying = false; return;
            }
            this.notif.success('Points payment request sent.');
            this.closeModal(); this.paying = false;
            this.router.navigate(['/passenger/subscriptions']); return;
          }
          window.location.href = res.checkoutUrl;
        },
        error: (err) => {
          if (this.payMode === 'CASH' && (err?.status === 500 || err?.status === 404 || err?.status === 400)) {
            fallbackToLegacyPayment();
            return;
          }
          const msg = err.error?.message
            || (Array.isArray(err.error?.errors) ? err.error.errors.join(' ') : null)
            || err.message;
          this.notif.error(
            msg && String(msg).trim() ? String(msg) : 'Error initiating Stripe payment.'
          );
          this.paying = false;
        }
      });
    };

    if (!code) { proceed(undefined); return; }

    this.api.getReductionByCode(code).subscribe({
      next: (r) => {
        if (this.isReductionExpired(r)) {
          this.clearPromo('Code expired or inactive.'); this.paying = false; return;
        }
        proceed(code);
      },
      error: () => { this.clearPromo('Invalid or expired code.'); this.paying = false; }
    });
  }

  closeModal() { this.showModal = false; }

  avatarClass(type: string) { return { 'cell-avatar-green': type === 'FREE', 'cell-avatar-blue': type === 'BASIC', 'cell-avatar-purple': type === 'PREMIUM' }; }
  typeClass(type: string)   { return { 'cell-tag-green': type === 'FREE', 'cell-tag-blue': type === 'BASIC', 'cell-tag-purple': type === 'PREMIUM' }; }
  transportIcon(t: string) {
    const u = t ? String(t).toUpperCase() : '';
    return { 'fa-bus': u === 'BUS', 'fa-subway': u === 'METRO', 'fa-train': u === 'TRAIN', 'fa-ship': u === 'BATTAH', 'fa-taxi': u === 'LOUAGE' };
  }
}
