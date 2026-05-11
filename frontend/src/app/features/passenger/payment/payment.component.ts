import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { SubscriptionResponse } from '../../../core/models/models';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:var(--bg-page)">
  <div class="card payment-card" style="text-align:center;padding:40px">

    <div *ngIf="loading" style="padding:20px">
      <i class="fas fa-spinner fa-spin" style="font-size:3rem;color:var(--brand-blue)"></i>
      <div style="margin-top:16px;color:var(--text-muted)">Confirming payment...</div>
    </div>

    <div *ngIf="!loading && subscription">
      <div style="width:72px;height:72px;background:#e8f5e9;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 20px">
        <i class="fas fa-check" style="font-size:2rem;color:#2e7d32"></i>
      </div>
      <h2 style="color:#2e7d32;margin-bottom:8px">Payment successful!</h2>
      <p style="color:var(--text-muted);margin-bottom:24px">Your subscription is now active.</p>

      <div style="background:var(--bg-header);border-radius:var(--radius-sm);padding:16px;margin-bottom:24px;text-align:left">
        <div class="flex-between mb-4">
          <span class="text-muted">Plan</span>
          <strong>{{ subscription.pricingPlan.nom }}</strong>
        </div>
        <div class="flex-between mb-4">
          <span class="text-muted">Valid until</span>
          <strong>{{ subscription.dateFin }}</strong>
        </div>
        <div class="flex-between mb-4">
          <span class="text-muted">Auto-renewal</span>
          <strong>{{ subscription.autoRenewal ? 'Yes (email reminders D-7 / D-1)' : 'No' }}</strong>
        </div>
        <div class="flex-between">
          <span class="text-muted">Points earned</span>
          <span class="cell-tag cell-tag-orange"><i class="fas fa-star"></i> +{{ subscription.pointsGagnes }} pts</span>
        </div>
      </div>

      <div style="display:flex;gap:12px;justify-content:center">
        <a routerLink="/passenger/subscriptions" class="btn btn-primary">
          <i class="fas fa-id-card"></i> My subscriptions
        </a>
        <a routerLink="/passenger/loyalty" class="btn btn-outline">
          <i class="fas fa-star"></i> My loyalty
        </a>
      </div>
    </div>

    <div *ngIf="!loading && error">
      <div style="width:72px;height:72px;background:#fce4ec;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 20px">
        <i class="fas fa-times" style="font-size:2rem;color:#c62828"></i>
      </div>
      <h2 style="color:#c62828;margin-bottom:8px">Confirmation error</h2>
      <p style="color:var(--text-muted);margin-bottom:24px">{{ error }}</p>
      <a routerLink="/passenger/plans" class="btn btn-primary">
        <i class="fas fa-arrow-left"></i> Back to plans
      </a>
    </div>

  </div>
</div>
  `
})
export class PaymentSuccessComponent implements OnInit {
  loading = true;
  subscription: SubscriptionResponse | null = null;
  error = '';

  constructor(private route: ActivatedRoute, private api: ApiService) {}

  ngOnInit() {
    const sessionId = this.route.snapshot.queryParams['session_id'];
    if (!sessionId) {
      this.error = 'Invalid payment session.';
      this.loading = false;
      return;
    }
    this.api.confirmPayment(sessionId).subscribe({
      next: (s) => { this.subscription = s; this.loading = false; },
      error: (e) => {
        this.error = e.error?.message || 'Unable to confirm payment.';
        this.loading = false;
      }
    });
  }
}

@Component({
  selector: 'app-payment-cancel',
  standalone: true,
  imports: [CommonModule, RouterLink],
  styleUrls: ['../../feature-styles.css'],
  template: `
<div style="min-height:100vh;display:flex;align-items:center;justify-content:center;background:var(--bg-page)">
  <div class="card payment-card" style="text-align:center;padding:40px">
    <div style="width:72px;height:72px;background:#fff8e1;border-radius:50%;display:flex;align-items:center;justify-content:center;margin:0 auto 20px">
      <i class="fas fa-times-circle" style="font-size:2rem;color:#f57f17"></i>
    </div>
    <h2 style="color:#f57f17;margin-bottom:8px">Payment cancelled</h2>
    <p style="color:var(--text-muted);margin-bottom:24px">You cancelled the payment. No amount has been charged.</p>
    <a routerLink="/passenger/plans" class="btn btn-primary">
      <i class="fas fa-arrow-left"></i> Back to plans
    </a>
  </div>
</div>
  `
})
export class PaymentCancelComponent {}
