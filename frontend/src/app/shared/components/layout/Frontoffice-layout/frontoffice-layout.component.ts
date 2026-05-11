import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ChatbotComponent } from '../../../chatbot/chatbot.component';

@Component({
  selector: 'app-frontoffice-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, RouterLink, RouterLinkActive, ChatbotComponent], // ← FIX 2
  styles: [`
    :host { display: block; }
    .fo-navbar {
      position: sticky; top: 0; height: 70px;
      background: #fff; border-bottom: 1px solid #e5e7eb;
      display: flex; align-items: center; justify-content: space-between;
      padding: 0 2rem; z-index: 1000;
      box-shadow: 0 1px 4px rgba(0,0,0,0.07);
    }
    .fo-brand { display:flex; align-items:center; gap:10px; text-decoration:none; }
    .fo-brand-icon {
      width:38px; height:38px;
      background: linear-gradient(135deg,#1a73e8,#0d47a1);
      border-radius:8px; display:flex; align-items:center; justify-content:center;
    }
    .fo-brand-icon i { color:#fff; font-size:18px; }
    .fo-brand-text { font-size:1.2rem; font-weight:700; color:#1a237e; }
    .fo-brand-text span { color:#1a73e8; }
    .fo-nav-links { display:flex; align-items:center; gap:4px; list-style:none; }
    .fo-nav-links a {
      padding:8px 16px; border-radius:6px; font-size:0.875rem;
      font-weight:500; color:#6b7280; text-decoration:none; transition:all 0.15s;
      display:flex; align-items:center; gap:6px;
    }
    .fo-nav-links a:hover, .fo-nav-links a.active-link { background:rgba(26,115,232,0.08); color:#1a73e8; }
    .fo-nav-actions { display:flex; align-items:center; gap:12px; }
    .fo-user-pill {
      display:flex; align-items:center; gap:8px;
      padding:6px 14px; background:#f3f4f6;
      border-radius:20px; font-size:0.82rem; font-weight:600; color:#374151;
    }
    .fo-user-avatar {
      width:28px; height:28px; border-radius:50%;
      background:linear-gradient(135deg,#1a73e8,#0d47a1);
      display:flex; align-items:center; justify-content:center;
      font-size:11px; font-weight:700; color:#fff;
    }
    .fo-logout-btn {
      padding:7px 16px; border-radius:6px; background:transparent;
      border:1.5px solid #1a73e8; color:#1a73e8;
      font-size:0.82rem; font-weight:600; cursor:pointer;
      transition:all 0.15s; display:flex; align-items:center; gap:6px; font-family:inherit;
    }
    .fo-logout-btn:hover { background:#1a73e8; color:#fff; }
    .fo-main {
      min-height: calc(100vh - 70px - 220px);
      padding: 32px 40px; max-width: 1300px;
      margin: 0 auto; width: 100%; box-sizing: border-box;
    }
    .fo-footer { background:#1f2937; color:rgba(255,255,255,0.7); padding:40px 2rem 20px; }
    .fo-footer-inner {
      max-width:1200px; margin:0 auto;
      display:grid; grid-template-columns:2fr 1fr 1fr; gap:2rem; margin-bottom:2rem;
    }
    .fo-footer-brand { display:flex; align-items:center; gap:10px; margin-bottom:12px; }
    .fo-footer-brand-icon {
      width:34px; height:34px; background:linear-gradient(135deg,#1a73e8,#0d47a1);
      border-radius:6px; display:flex; align-items:center; justify-content:center;
    }
    .fo-footer-brand-icon i { color:#fff; font-size:16px; }
    .fo-footer-brand-text { font-size:1.1rem; font-weight:700; color:#fff; }
    .fo-footer-desc { font-size:0.82rem; line-height:1.6; color:rgba(255,255,255,0.55); }
    .fo-footer-col h6 {
      font-size:0.72rem; font-weight:700; text-transform:uppercase;
      letter-spacing:0.8px; color:#fff; margin-bottom:12px;
    }
    .fo-footer-links { list-style:none; display:flex; flex-direction:column; gap:8px; }
    .fo-footer-links a { font-size:0.82rem; color:rgba(255,255,255,0.55); text-decoration:none; transition:color 0.15s; }
    .fo-footer-links a:hover { color:#fff; }
    .fo-footer-contact-item {
      display:flex; align-items:center; gap:8px;
      font-size:0.82rem; color:rgba(255,255,255,0.55); margin-bottom:8px;
    }
    .fo-footer-contact-item i { color:#1a73e8; width:14px; }
    .fo-footer-bottom {
      max-width:1200px; margin:0 auto; padding-top:16px;
      border-top:1px solid rgba(255,255,255,0.08);
      display:flex; align-items:center; justify-content:space-between;
      font-size:0.78rem; color:rgba(255,255,255,0.35);
    }
    .fo-footer-social { display:flex; gap:10px; }
    .fo-social-icon {
      width:30px; height:30px; border-radius:50%; background:rgba(255,255,255,0.08);
      display:flex; align-items:center; justify-content:center;
      color:rgba(255,255,255,0.5); font-size:12px; text-decoration:none; transition:all 0.15s;
    }
    .fo-social-icon:hover { background:#1a73e8; color:#fff; }
    @media (max-width:768px) {
      .fo-main { padding:20px; }
      .fo-nav-links { display:none; }
      .fo-footer-inner { grid-template-columns:1fr; }
    }
  `],
  template: `
    <!-- NAVBAR -->
    <nav class="fo-navbar">
      <a class="fo-brand" routerLink="/passenger/plans">
        <div class="fo-brand-icon"><i class="fas fa-bus-alt"></i></div>
        <span class="fo-brand-text">Transit<span>TN</span></span>
      </a>
      <ul class="fo-nav-links">
        <li><a routerLink="/passenger/plans"         routerLinkActive="active-link"><i class="fas fa-bus"></i> Plans</a></li>
        <li><a routerLink="/passenger/subscriptions" routerLinkActive="active-link"><i class="fas fa-id-card"></i> My Subscriptions</a></li>
        <li><a routerLink="/passenger/loyalty"       routerLinkActive="active-link"><i class="fas fa-star"></i> My Loyalty</a></li>
      </ul>
      <div class="fo-nav-actions">
        <div class="fo-user-pill">
          <div class="fo-user-avatar">{{ initials }}</div>
          {{ userName }}
        </div>
        <button class="fo-logout-btn" (click)="logout()">
          <i class="fas fa-sign-out-alt"></i> Log out
        </button>
      </div>
    </nav>

    <!-- MAIN CONTENT -->
    <main class="fo-main">
      <router-outlet></router-outlet>
    </main>

    <!-- FOOTER -->
    <footer class="fo-footer">
      <div class="fo-footer-inner">
        <div>
          <div class="fo-footer-brand">
            <div class="fo-footer-brand-icon"><i class="fas fa-bus-alt"></i></div>
            <span class="fo-footer-brand-text">TransitTN</span>
          </div>
          <p class="fo-footer-desc">
            Simplifying public transport subscriptions for Tunisia's passengers.
            Choose your plan, earn loyalty points, and travel smarter.
          </p>
        </div>
        <div class="fo-footer-col">
          <h6>Navigation</h6>
          <ul class="fo-footer-links">
            <li><a routerLink="/passenger/plans">Available Plans</a></li>
            <li><a routerLink="/passenger/subscriptions">My Subscriptions</a></li>
            <li><a routerLink="/passenger/loyalty">Loyalty Program</a></li>
          </ul>
        </div>
        <div class="fo-footer-col">
          <h6>Contact</h6>
          <div class="fo-footer-contact-item"><i class="fas fa-map-marker-alt"></i> Tunis, Tunisia</div>
          <div class="fo-footer-contact-item"><i class="fas fa-envelope"></i> support&#64;transittn.tn</div>
          <div class="fo-footer-contact-item"><i class="fas fa-phone"></i> +216 71 000 000</div>
        </div>
      </div>
      <div class="fo-footer-bottom">
        <span>© 2026 TransitTN. All rights reserved.</span>
        <div class="fo-footer-social">
          <a class="fo-social-icon"><i class="fab fa-facebook-f"></i></a>
          <a class="fo-social-icon"><i class="fab fa-twitter"></i></a>
          <a class="fo-social-icon"><i class="fab fa-linkedin-in"></i></a>
          <a class="fo-social-icon"><i class="fab fa-instagram"></i></a>
        </div>
      </div>
    </footer>

    <!-- ── FIX 3 : Chatbot ajouté ── -->
    <app-chatbot></app-chatbot>
  `
})
export class FrontofficeLayoutComponent implements OnInit {
  initials = 'P';
  userName  = '';

  constructor(private auth: AuthService, private router: Router) {}

  ngOnInit(): void {
    const user = this.auth.getUser();
    if (user) {
      this.userName = user.name || user.username || 'Passenger';
      const parts   = this.userName.trim().split(' ');
      this.initials = parts.length >= 2
        ? (parts[0][0] + parts[1][0]).toUpperCase()
        : parts[0][0].toUpperCase();
    }
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}