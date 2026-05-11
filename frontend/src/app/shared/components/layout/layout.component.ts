import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService, Notification } from '../../../core/services/notification.service';
import { ApiService } from '../../../core/services/api.service';
import { RoleEnum } from '../../../core/models/models';
import { forkJoin } from 'rxjs';
import { ChatbotComponent } from '../../chatbot/chatbot.component';

interface MlAlert { icon: string; iconColor: string; message: string; }

@Component({
  selector: 'app-layout',
  standalone: true,
  // ── FIX 1 : ChatbotComponent ajouté dans imports ──
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive, ChatbotComponent],
  styles: [`
    .bell-wrapper { position:relative; }
    .bell-btn {
      width:34px; height:34px; border-radius:var(--radius-sm);
      background:var(--bg-header); border:none; cursor:pointer;
      display:flex; align-items:center; justify-content:center;
      color:var(--text-muted); font-size:14px; position:relative; transition:background 0.15s;
    }
    .bell-btn:hover { background:var(--border-mid); }
    .bell-count {
      position:absolute; top:-4px; right:-4px;
      width:17px; height:17px; border-radius:50%;
      background:#c62828; color:#fff; font-size:0.6rem; font-weight:700;
      display:flex; align-items:center; justify-content:center; border:2px solid #fff;
    }
    .bell-dropdown {
      position:absolute; top:calc(100% + 8px); right:0;
      width:320px; background:#fff; border-radius:12px;
      box-shadow:0 8px 30px rgba(0,0,0,0.15); border:1px solid var(--border-mid);
      z-index:9999; overflow:hidden;
    }
    .bell-dh {
      padding:12px 16px; border-bottom:1px solid var(--border-light);
      display:flex; justify-content:space-between; align-items:center;
    }
    .bell-dh-title { font-size:0.85rem; font-weight:700; color:var(--brand-primary); }
    .bell-clear { font-size:0.72rem; color:var(--brand-blue); cursor:pointer;
                  background:none; border:none; font-family:inherit; font-weight:600; }
    .bell-item {
      display:flex; align-items:flex-start; gap:10px;
      padding:10px 16px; border-bottom:1px solid var(--border-light); transition:background 0.12s;
    }
    .bell-item:hover { background:var(--bg-header); }
    .bell-item:last-child { border-bottom:none; }
    .bell-ico {
      width:28px; height:28px; border-radius:6px; flex-shrink:0; margin-top:2px;
      display:flex; align-items:center; justify-content:center; font-size:12px;
    }
    .bi-red    { background:#fce4ec; color:#c62828; }
    .bi-blue   { background:#e3f2fd; color:#1a73e8; }
    .bi-green  { background:#e8f5e9; color:#2e7d32; }
    .bi-orange { background:#fff8e1; color:#f57f17; }
    .bell-msg  { font-size:0.78rem; color:#333; line-height:1.4; }
    .bell-empty { padding:20px; text-align:center; font-size:0.82rem; color:#999; }
    .bell-empty i { font-size:1.8rem; display:block; margin-bottom:8px; color:#ddd; }
  `],
  template: `
    <!-- NOTIFICATIONS TOAST -->
    <div class="notification-container">
      <div *ngFor="let n of notifications" class="notification notification-{{n.type}}">
        <i class="fas" [ngClass]="{
          'fa-check-circle':         n.type==='success',
          'fa-times-circle':         n.type==='error',
          'fa-info-circle':          n.type==='info',
          'fa-exclamation-triangle': n.type==='warning'
        }"></i>
        {{ n.message }}
      </div>
    </div>

    <!-- SIDEBAR -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="sidebar-brand-icon"><i class="fas fa-bus-alt"></i></div>
        <span class="sidebar-brand-text">TransitTN</span>
      </div>

      <ng-container *ngIf="isOperator">
        <div class="sidebar-section-label">Operator</div>
        <ul class="sidebar-nav">
          <li><a routerLink="/operator/dashboard"      routerLinkActive="active" class="sidebar-link"><i class="fas fa-tachometer-alt"></i> Dashboard</a></li>
          <li><a routerLink="/operator/pricing-plans"  routerLinkActive="active" class="sidebar-link"><i class="fas fa-tags"></i> Pricing Plans</a></li>
          <li><a routerLink="/operator/reductions"     routerLinkActive="active" class="sidebar-link"><i class="fas fa-percent"></i> Discounts</a></li>
          <li><a routerLink="/operator/subscriptions"  routerLinkActive="active" class="sidebar-link"><i class="fas fa-id-card"></i> Subscriptions</a></li>
          <li><a routerLink="/operator/loyalty"        routerLinkActive="active" class="sidebar-link"><i class="fas fa-star"></i> Loyalty</a></li>
        </ul>
        <div class="sidebar-section-label">AI & ML</div>
        <ul class="sidebar-nav">
          <li><a routerLink="/operator/ml" routerLinkActive="active" class="sidebar-link"><i class="fas fa-brain"></i> ML Analysis</a></li>
        </ul>
      </ng-container>

      <ng-container *ngIf="isPassenger">
        <div class="sidebar-section-label">Passenger</div>
        <ul class="sidebar-nav">
          <li><a routerLink="/passenger/plans"         routerLinkActive="active" class="sidebar-link"><i class="fas fa-bus"></i> Available Plans</a></li>
          <li><a routerLink="/passenger/subscriptions" routerLinkActive="active" class="sidebar-link"><i class="fas fa-id-card"></i> My Subscriptions</a></li>
          <li><a routerLink="/passenger/loyalty"       routerLinkActive="active" class="sidebar-link"><i class="fas fa-star"></i> My Loyalty</a></li>
        </ul>
      </ng-container>
    </aside>

    <!-- TOPBAR -->
    <nav class="topbar">
      <div class="topbar-title">
        <i class="fas fa-th-large" style="color:#1a73e8;margin-right:8px;"></i>
        TransitTN Backoffice
      </div>
      <div class="topbar-actions">

        <!-- BELL -->
        <div class="bell-wrapper" *ngIf="isOperator">
          <button class="bell-btn" (click)="toggleBell()">
            <i class="fas fa-bell"></i>
            <span class="bell-count" *ngIf="mlAlerts.length > 0">{{ mlAlerts.length }}</span>
          </button>
          <div class="bell-dropdown" *ngIf="showBell">
            <div class="bell-dh">
              <span class="bell-dh-title">ML Alerts ({{ mlAlerts.length }})</span>
              <button class="bell-clear" (click)="clearBell()">Clear all</button>
            </div>
            <div *ngIf="mlAlerts.length === 0" class="bell-empty">
              <i class="fas fa-check-circle"></i> No alerts
            </div>
            <div *ngFor="let a of mlAlerts" class="bell-item">
              <div class="bell-ico" [ngClass]="a.iconColor">
                <i class="fas" [ngClass]="a.icon"></i>
              </div>
              <div class="bell-msg">{{ a.message }}</div>
            </div>
          </div>
        </div>

        <div class="topbar-user">
          <div class="topbar-avatar">{{ initials }}</div>
          <div>
            <div class="topbar-user-name">{{ userName }}</div>
            <div class="topbar-user-role">{{ userRole }}</div>
          </div>
        </div>
        <button class="btn btn-outline btn-sm" (click)="logout()">
          <i class="fas fa-sign-out-alt"></i> Log out
        </button>
      </div>
    </nav>

    <!-- MAIN -->
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>

    <!-- ── FIX 2 : Chatbot ajouté ── -->
    <app-chatbot></app-chatbot>
  `
})
export class LayoutComponent implements OnInit {
  isOperator  = false;
  isPassenger = false;
  initials    = 'U';
  userName    = '';
  userRole    = '';
  notifications: Notification[] = [];
  showBell    = false;
  mlAlerts: MlAlert[] = [];

  constructor(
    private auth:         AuthService,
    private router:       Router,
    private notifService: NotificationService,
    private api:          ApiService
  ) {}

  ngOnInit() {
    const user = this.auth.getUser();
    if (user) {
      this.isOperator  = user.role === RoleEnum.OPERATOR || user.role === RoleEnum.ADMIN;
      this.isPassenger = user.role === RoleEnum.PASSENGER;
      this.initials    = this.auth.getInitials();
      this.userName    = user.name || user.username;
      this.userRole    = user.role;
    }

    this.notifService.getNotifications().subscribe((n: Notification) => {
      this.notifications.push(n);
      setTimeout(() => {
        this.notifications = this.notifications.filter(x => x.id !== n.id);
      }, 4000);
    });

    if (this.isOperator) {
      const opId = this.auth.getUserId();
      if (opId != null) this.loadMlAlerts(opId);
    }
  }

  private loadMlAlerts(opId: number): void {
    forkJoin({
      churnList: this.api.predictChurnAll(),
      subs:      this.api.getSubscriptionsByOperator(opId),
    }).subscribe({
      next: ({ churnList, subs }) => {
        const usernames = new Set(subs.map(s => s.passengerUsername).filter((u): u is string => !!u));
        const myChurn   = churnList.filter(c => usernames.has(c.username));
        const highRisk  = myChurn.filter(c => c.riskLevel === 'HIGH');
        const upgrade   = myChurn.filter(c =>
          c.suggestedAction?.toLowerCase().includes('premium') ||
          c.suggestedAction?.toLowerCase().includes('upgrade')
        );
        const alerts: MlAlert[] = [];
        for (const c of highRisk.slice(0, 3)) {
          alerts.push({ icon: 'fa-exclamation-triangle', iconColor: 'bi-red',
            message: `${c.username} — HIGH churn risk (${(c.churnProbability * 100).toFixed(0)}%)` });
        }
        if (upgrade.length > 0) {
          alerts.push({ icon: 'fa-arrow-circle-up', iconColor: 'bi-blue',
            message: `${upgrade.length} passenger(s) ready for PREMIUM upgrade` });
        }
        if (highRisk.length > 3) {
          alerts.push({ icon: 'fa-users', iconColor: 'bi-orange',
            message: `${highRisk.length - 3} more high-risk passengers — check ML Analysis` });
        }
        this.mlAlerts = alerts;
      },
      error: () => {}
    });
  }

  toggleBell() { this.showBell = !this.showBell; }
  clearBell()  { this.mlAlerts = []; this.showBell = false; }
  logout()     { this.auth.logout(); }
}