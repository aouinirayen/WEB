import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';

@Component({
  selector: 'app-backoffice-sidebar',
  templateUrl: './backoffice-sidebar.component.html',
  styleUrls: ['./backoffice-sidebar.component.css']
})
export class BackofficeSidebarComponent implements OnInit {
  userRole: string = '';
  sidebarSections: any[] = [];

  adminSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fa-th-large', label: 'Dashboard', path: '/admin/dashboard' },
        { icon: 'fa-chart-line', label: 'Platform Analytics', path: '/admin/stats-dashboard' }
      ]
    },
    {
      label: 'Fleet',
      items: [
        { icon: 'fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fa-road', label: 'Trips', path: '/admin/trips' }
      ]
    },
    {
      label: 'Admin',
      items: [
        { icon: 'fa-users', label: 'User Management', path: '/admin/users' },
        { icon: 'fa-file', label: 'Document Management', path: '/admin/documents' },
        { icon: 'fa-calendar-times', label: 'Document Expiry Alerts', path: '/admin/expiry-alerts' },
        { icon: 'fa-history', label: 'Audit Log', path: '/admin/audit-log' },
        { icon: 'fa-brain', label: 'AI Stats & Models', path: '/admin/ai-stats' }
      ]
    },
    {
      label: 'Ticket & Transport',
      items: [
        { icon: 'fa-car', label: 'Carpools', path: '/admin/ticket/covoiturages' },
        { icon: 'fa-calendar', label: 'Reservations', path: '/admin/ticket/reservations' },
        { icon: 'fa-ticket', label: 'Tickets', path: '/admin/ticket/tickets' }
      ]
    },
    {
      label: 'Management',
      items: [
        { icon: 'fa-building', label: 'Organizations', path: '/admin/organizations' },
        { icon: 'fa-handshake', label: 'Partners', path: '/admin/partners' },
        { icon: 'fa-file-text', label: 'Contracts', path: '/admin/contracts' },
        { icon: 'fa-bell', label: 'Contract Reminders', path: '/admin/contracts/reminders' }
      ]
    }
  ];

  operatorSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fa-th-large', label: 'Dashboard', path: '/operator/dashboard' },
        { icon: 'fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fa-road', label: 'Trips', path: '/admin/trips' },
        { icon: 'fa-car', label: 'Carpools', path: '/ticket/covoiturages' },
        { icon: 'fa-calendar', label: 'Reservations', path: '/ticket/reservations' },
        { icon: 'fa-ticket', label: 'Tickets', path: '/ticket/tickets' }
      ]
    },
    {
      label: 'Subscriptions & Plans',
      items: [
        { icon: 'fa-tags', label: 'Pricing Plans', path: '/operator/pricing-plans' },
        { icon: 'fa-id-card', label: 'Subscriptions', path: '/operator/subscriptions' },
        { icon: 'fa-percent', label: 'Discounts', path: '/operator/reductions' }
      ]
    },
    {
      label: 'Analytics & Loyalty',
      items: [
        { icon: 'fa-star', label: 'Loyalty Program', path: '/operator/loyalty' },
        { icon: 'fa-brain', label: 'ML Analysis', path: '/operator/ml' }
      ]
    }
  ];

  agentSections = [
    {
      label: 'Main',
      items: [
        { icon: 'fa-th-large', label: 'Dashboard', path: '/admin/dashboard' },
        { icon: 'fa-bus', label: 'Vehicles', path: '/admin/vehicles' },
        { icon: 'fa-wrench', label: 'Maintenance', path: '/admin/maintenance' },
        { icon: 'fa-user-tie', label: 'Drivers', path: '/admin/drivers' },
        { icon: 'fa-gas-pump', label: 'Fuel Logs', path: '/admin/fuel-logs' },
        { icon: 'fa-cogs', label: 'Spare Parts', path: '/admin/spare-parts' },
        { icon: 'fa-screwdriver-wrench', label: 'Part Usage', path: '/admin/part-usage' },
        { icon: 'fa-chart-line', label: 'Predictions', path: '/admin/predictions' },
        { icon: 'fa-map', label: 'Route Map', path: '/admin/route-map' },
        { icon: 'fa-route', label: 'Lines', path: '/admin/lines' },
        { icon: 'fa-map-marker-alt', label: 'Stops', path: '/admin/stops' },
        { icon: 'fa-clock', label: 'Schedules', path: '/admin/schedules' },
        { icon: 'fa-road', label: 'Trips', path: '/admin/trips' }
      ]
    }
  ];

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    const user = this.authService.currentUserValue;
    this.userRole = user?.role?.toUpperCase() || '';

    this.sidebarSections = this.userRole === 'OPERATOR' ? this.operatorSections : this.adminSections;
    if (this.userRole === 'OPERATOR') {
      this.sidebarSections = this.operatorSections;
    } else if (this.userRole === 'AGENT') {
      this.sidebarSections = this.agentSections;
    } else {
      this.sidebarSections = this.adminSections;
    }
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  isActive(path: string): boolean {
    return this.router.url.includes(path);
  }
}
