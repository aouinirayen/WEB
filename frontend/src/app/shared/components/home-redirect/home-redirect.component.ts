import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

/** Redirige la racine de l’app (sous layout) vers l’espace opérateur ou passager selon le rôle. */
@Component({
  standalone: true,
  template: '',
})
export class HomeRedirectComponent implements OnInit {
  constructor(
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {}

  ngOnInit(): void {
    const target =
      this.auth.isOperator() || this.auth.isAdmin()
        ? '/operator/dashboard'
        : '/passenger/plans';
    void this.router.navigateByUrl(target, { replaceUrl: true });
  }
}
