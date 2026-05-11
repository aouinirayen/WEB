import { Injectable } from '@angular/core';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private router: Router,
    private authService: AuthService
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const isLoggedIn = this.authService.isLoggedIn();

    if (!isLoggedIn) {
      // Not logged in, redirect to login page
      this.router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    // Check if specific roles are required for this route
    const requiredRoles = route.data['roles'] as string[];

    if (requiredRoles && requiredRoles.length > 0) {
      const currentUser = this.authService.currentUserValue;

      if (!currentUser) {
        this.router.navigate(['/login']);
        return false;
      }

      const userRole = currentUser.role.toUpperCase();
      const hasRequiredRole = requiredRoles.some(role => role.toUpperCase() === userRole);

      if (!hasRequiredRole) {
        // User doesn't have required role, redirect to access denied page
        this.router.navigate(['/access-denied']);
        return false;
      }
    }

    return true;
  }
}
