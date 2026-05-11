import { Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login.component';
import { LayoutComponent } from './shared/components/layout/layout.component';
import { HomeRedirectComponent } from './shared/components/home-redirect/home-redirect.component';
import { AuthGuard, OperatorGuard, PassengerGuard } from './core/guards/auth.guard';

// ── NEW : Front-office layout for passenger ──
import { FrontofficeLayoutComponent } from './shared/components/layout/Frontoffice-layout/frontoffice-layout.component';

// Operator
import { OperatorDashboardComponent } from './features/operator/dashboard/operator-dashboard.component';
import { PricingPlanComponent } from './features/operator/pricing-plan/pricing-plan.component';
import { ReductionComponent } from './features/operator/reduction/reduction.component';
import { MlDashboardComponent } from './features/operator/ml-dashboard/ml-dashboard.component';
import { OperatorSubscriptionsComponent } from './features/operator/subscriptions/operator-subscriptions.component';
import { OperatorLoyaltyComponent } from './features/operator/loyalty/operator-loyalty.component';

// Passenger
import { PassengerPlansComponent } from './features/passenger/plans/passenger-plans.component';
import { PassengerSubscriptionsComponent } from './features/passenger/subscription/passenger-subscriptions.component';
import { PassengerLoyaltyComponent } from './features/passenger/loyalty/passenger-loyalty.component';

// Payment
import { PaymentSuccessComponent, PaymentCancelComponent } from './features/passenger/payment/payment.component';

export const routes: Routes = [

  // 🔓 Public access
  { path: 'login', component: LoginComponent },
  { path: 'payment/success', component: PaymentSuccessComponent },
  { path: 'payment/cancel', component: PaymentCancelComponent },

  // ── OPERATOR — backoffice layout (sidebar) ──────────────────────────────
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard, OperatorGuard],
    children: [
      { path: '', pathMatch: 'full', component: HomeRedirectComponent },
      { path: 'operator/dashboard',     component: OperatorDashboardComponent },
      { path: 'operator/pricing-plans', component: PricingPlanComponent },
      { path: 'operator/reductions',    component: ReductionComponent },
      { path: 'operator/subscriptions', component: OperatorSubscriptionsComponent },
      { path: 'operator/loyalty',       component: OperatorLoyaltyComponent },
      { path: 'operator/ml',            component: MlDashboardComponent },
    ]
  },

  // ── PASSENGER — front-office layout (header/footer public) ─────────────
  {
    path: '',
    component: FrontofficeLayoutComponent,
    canActivate: [AuthGuard, PassengerGuard],
    children: [
      { path: 'passenger/plans',         component: PassengerPlansComponent },
      { path: 'passenger/subscriptions', component: PassengerSubscriptionsComponent },
      { path: 'passenger/loyalty',       component: PassengerLoyaltyComponent },
    ]
  },

  // fallback
  { path: '**', redirectTo: 'login' }
];