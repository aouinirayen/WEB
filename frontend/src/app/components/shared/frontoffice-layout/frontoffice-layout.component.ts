import { Component, OnDestroy, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { Subject, filter, takeUntil } from 'rxjs';

@Component({
  selector: 'app-frontoffice-layout',
  templateUrl: './frontoffice-layout.component.html',
  styleUrls: ['./frontoffice-layout.component.css']
})
export class FrontofficeLayoutComponent implements OnInit, OnDestroy {
  currentRoute = '';
  private destroy$ = new Subject<void>();

  constructor(private router: Router) {}

  ngOnInit(): void {
    this.currentRoute = this.router.url;
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroy$)
      )
      .subscribe((event) => {
        this.currentRoute = event.urlAfterRedirects;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  useIncidentsFullLayout(): boolean {
    return this.currentRoute.startsWith('/incidents/create');
  }

  hideNavbarAndFooter(): boolean {
    return this.currentRoute.startsWith('/login')
      || this.currentRoute.startsWith('/register')
      || this.currentRoute.startsWith('/forgot-password')
      || this.currentRoute.startsWith('/reset-password');
  }
}
