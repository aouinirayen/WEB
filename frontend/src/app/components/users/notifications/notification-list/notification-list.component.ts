import { Component, OnInit } from '@angular/core';
import { IncidentNotificationService } from '../../../../services/incident-notification/incident-notification.service';
import { AppNotification } from '../../../../models/incident-notification.model';

@Component({
  selector: 'app-notification-list',
  templateUrl: './notification-list.component.html',
  styleUrls: ['./notification-list.component.css']
})
export class NotificationListComponent implements OnInit {
  notifications: AppNotification[] = [];
  filteredNotifications: AppNotification[] = [];
  isLoading = false;
  errorMessage = '';
  searchTerm = '';
  selectedState: 'ALL' | 'UNREAD' | 'READ' = 'ALL';

  constructor(private incidentNotificationService: IncidentNotificationService) {}

  ngOnInit(): void {
    this.loadNotifications();
    this.incidentNotificationService.notifications$.subscribe((data) => {
      this.notifications = data;
      this.applyFilters();
    });
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.incidentNotificationService.getMyNotifications().subscribe({
      next: () => {
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.errorMessage = 'Unable to load notifications right now.';
      }
    });
  }

  markAsRead(notification: AppNotification): void {
    if (notification.status === 'READ') {
      return;
    }
    this.incidentNotificationService.markNotificationAsRead(notification.id).subscribe();
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onStateFilterChange(): void {
    this.applyFilters();
  }

  asDate(value: string): Date {
    return new Date(value);
  }

  extractSeverity(title: string, message: string): 'HIGH' | 'MEDIUM' | 'LOW' {
    const text = `${title || ''} ${message || ''}`.toLowerCase();
    if (
      /critical alert|high|fire|gas|chemical|toxic|collision|crash|accident|derail|flood|evacuate/.test(text)
    ) {
      return 'HIGH';
    }
    if (
      /warning|medium|delay|broken|medical|injury|collapse|crowd|failure|signal|power|outage|technical|security|threat|fight/.test(text)
    ) {
      return 'MEDIUM';
    }
    return 'LOW';
  }

  extractDelay(title: string, message: string): string {
    const text = `${title || ''} ${message || ''}`;
    const match = text.match(
      /(?:estimated\s+(?:delay|resolution)[:\s]*|est\.\s*delay[:\s]*)(\d+)\s*(?:min|minutes)?/i
    );
    if (match) return `${match[1]} min`;

    const severity = this.extractSeverity(title, message);
    if (severity === 'HIGH') return '60 min';
    if (severity === 'MEDIUM') return '20 min';
    return '5 min';
  }

  private applyFilters(): void {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();
    this.filteredNotifications = this.notifications.filter((item) => {
      const matchesState =
        this.selectedState === 'ALL' ||
        (this.selectedState === 'READ' && item.status === 'READ') ||
        (this.selectedState === 'UNREAD' && item.status !== 'READ');

      const searchableText = `${item.title} ${item.message}`.toLowerCase();
      const matchesSearch = !normalizedSearch || searchableText.includes(normalizedSearch);

      return matchesState && matchesSearch;
    });
  }
}
