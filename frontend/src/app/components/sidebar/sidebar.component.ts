import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  userRole: string = '';
  sidebarSections: any[] = [];

  adminSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fas fa-th-large', label: 'Dashboard', path: '/admin/dashboard' }
      ]
    },
    {
      label: 'Fleet',
      items: [
        { icon: 'fas fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fas fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fas fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fas fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fas fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fas fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fas fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fas fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fas fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fas fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fas fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fas fa-road', label: 'Trips', path: '/admin/trips' }
      ]
    },
    {
      label: 'Admin',
      items: [
        { icon: 'fas fa-users', label: 'User Management', path: '/admin/users' },
        { icon: 'fas fa-file', label: 'Document Management', path: '/admin/documents' },
        { icon: 'fas fa-calendar-times', label: 'Document Expiry Alerts', path: '/admin/expiry-alerts' },
        { icon: 'fas fa-history', label: 'Audit Log', path: '/admin/audit-log' },
        { icon: 'fas fa-brain', label: 'AI Stats & Models', path: '/admin/ai-stats' }
      ]
    },
    {
      label: 'Ticket & Transport',
      items: [
        { icon: 'fas fa-car', label: 'Carpools', path: '/admin/ticket/covoiturages' },
        { icon: 'fas fa-calendar', label: 'Reservations', path: '/admin/ticket/reservations' },
        { icon: 'fas fa-ticket', label: 'Tickets', path: '/admin/ticket/tickets' }
      ]
    },
    {
      label: 'Management',
      items: [
        { icon: 'fas fa-building', label: 'Organizations', path: '/admin/organizations' },
        { icon: 'fas fa-handshake', label: 'Partners', path: '/admin/partners' },
        { icon: 'fas fa-file-lines', label: 'Contracts', path: '/admin/contracts' },
        { icon: 'fas fa-bell', label: 'Contract Reminders', path: '/admin/contracts/reminders' }
      ]
    }
  ];

  operatorSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fas fa-th-large', label: 'Dashboard', path: '/admin/dashboard' },
        { icon: 'fas fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fas fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fas fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fas fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fas fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fas fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fas fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fas fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fas fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fas fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fas fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fas fa-road', label: 'Trips', path: '/admin/trips' },
        { icon: 'fas fa-car', label: 'Carpools', path: '/admin/ticket/covoiturages' },
        { icon: 'fas fa-calendar', label: 'Reservations', path: '/admin/ticket/reservations' },
        { icon: 'fas fa-ticket', label: 'Tickets', path: '/admin/ticket/tickets' }
      ]
    },
    {
      label: 'Subscriptions & Plans',
      items: [
        { icon: 'fas fa-tags', label: 'Pricing Plans', path: '/operator/pricing-plans' },
        { icon: 'fas fa-id-card', label: 'Subscriptions', path: '/operator/subscriptions' },
        { icon: 'fas fa-percent', label: 'Discounts', path: '/operator/reductions' }
      ]
    },
    {
      label: 'Analytics & Loyalty',
      items: [
        { icon: 'fas fa-star', label: 'Loyalty Program', path: '/operator/loyalty' },
        { icon: 'fas fa-brain', label: 'ML Analysis', path: '/operator/ml' }
      ]
    }
  ];

  agentSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fas fa-th-large', label: 'Dashboard', path: '/admin/dashboard' },
        { icon: 'fas fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fas fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fas fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fas fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fas fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fas fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fas fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fas fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fas fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fas fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fas fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fas fa-road', label: 'Trips', path: '/admin/trips' }
      ]
    }
  ];

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    this.userRole = (user?.role?.toString() ?? '').toUpperCase();
    if (this.userRole === 'OPERATOR') {
      this.sidebarSections = this.operatorSections;
      return;
    }

    if (this.userRole === 'AGENT') {
      this.sidebarSections = this.agentSections;
      return;
    }

    this.sidebarSections = this.adminSections;
  }

  navigateTo(path: string): void {
    void this.router.navigate([path]);
  }

  isActive(path: string): boolean {
    return this.router.url.includes(path);
  }
}