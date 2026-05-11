import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../../../services/auth/auth.service';
import { IncidentNotificationService } from '../../../../services/incident-notification/incident-notification.service';
import { IncidentSummary } from '../../../../models/incident-notification.model';

@Component({
  selector: 'app-incident-management',
  templateUrl: './incident-management.component.html',
  styleUrls: ['./incident-management.component.css']
})
export class IncidentManagementComponent implements OnInit {
  incidents: IncidentSummary[] = [];
  filteredIncidents: IncidentSummary[] = [];
  isLoading = false;
  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';

  readonly severityOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  searchTerm = '';
  selectedSeverityFilter = 'ALL';

  constructor(
    private router: Router,
    private authService: AuthService,
    private incidentNotificationService: IncidentNotificationService
  ) {}

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.isLoading = true;
    this.incidentNotificationService.getAllIncidents().subscribe({
      next: (items) => {
        this.incidents = items;
        this.applyFilters();
        this.isLoading = false;
      },
      error: () => {
        this.showFeedback('Unable to load incidents.', 'error');
        this.isLoading = false;
      }
    });
  }

  goToCreateIncident(): void {
    this.router.navigate(['/incidents/create']);
  }

  isAgent(): boolean {
    return this.authService.currentUserValue?.role?.toUpperCase() === 'AGENT';
  }

  onSearchChange(): void {
    this.applyFilters();
  }

  onSeverityFilterChange(): void {
    this.applyFilters();
  }

  private applyFilters(): void {
    const normalizedSearch = this.searchTerm.trim().toLowerCase();
    this.filteredIncidents = this.incidents.filter((incident) => {
      const matchesSeverity =
        this.selectedSeverityFilter === 'ALL' || incident.severity === this.selectedSeverityFilter;
      const searchableText = `${incident.title} ${incident.location} ${incident.reportedByName || ''}`.toLowerCase();
      const matchesSearch = !normalizedSearch || searchableText.includes(normalizedSearch);
      return matchesSeverity && matchesSearch;
    });
  }

  private showFeedback(message: string, type: 'success' | 'error'): void {
    this.feedbackMessage = message;
    this.feedbackType = type;
  }
}
