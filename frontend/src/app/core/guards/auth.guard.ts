import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (this.auth.isLoggedIn()) return true;
    this.router.navigate(['/login']);
    return false;
  }
}

@Injectable({ providedIn: 'root' })
export class OperatorGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (this.auth.isOperator() || this.auth.isAdmin()) return true;
    this.router.navigate(['/passenger/plans']);
    return false;
  }
}

@Injectable({ providedIn: 'root' })
export class PassengerGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}
  canActivate(): boolean {
    if (this.auth.isPassenger()) return true;
    if (this.auth.isOperator() || this.auth.isAdmin()) {
      this.router.navigate(['/operator/dashboard']);
      return false;
    }
    this.router.navigate(['/login']);
    return false;
  }
}
