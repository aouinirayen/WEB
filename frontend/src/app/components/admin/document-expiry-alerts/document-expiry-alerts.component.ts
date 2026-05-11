import { Component, OnInit, OnDestroy } from '@angular/core';
import { DocumentExpiryService, ExpiryAlertResponse } from '../../../services/documents/document-expiry.service';
import { DocumentService } from '../../../services/documents/document.service';
import { ToastService } from '../../../services/shared/toast.service';
import { LegalDocument } from '../../../models';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime } from 'rxjs/operators';

@Component({
  selector: 'app-document-expiry-alerts',
  templateUrl: './document-expiry-alerts.component.html',
  styleUrls: ['./document-expiry-alerts.component.css']
})
export class DocumentExpiryAlertsComponent implements OnInit, OnDestroy {
  private readonly fileServerUrl = 'http://localhost:8081/pidev-uploads/';

  criticalDocuments: LegalDocument[] = [];
  upcomingDocuments: LegalDocument[] = [];
  expiredDocuments: LegalDocument[] = [];

  // Filtered versions
  filteredCriticalDocuments: LegalDocument[] = [];
  filteredUpcomingDocuments: LegalDocument[] = [];
  filteredExpiredDocuments: LegalDocument[] = [];

  loading = false;
  error: string | null = null;

  expiryStats: any = null;
  activeTab: 'critical' | 'upcoming' | 'expired' = 'critical';

  // Search and filter properties
  searchQuery: string = '';
  selectedDocumentType: string = '';
  selectedStatus: string = '';

  // For search debouncing
  private searchSubject = new Subject<string>();
  private destroy$ = new Subject<void>();

  // Expose service for template
  documentExpiryService: DocumentExpiryService;

  constructor(
    private _documentExpiryService: DocumentExpiryService,
    private documentService: DocumentService,
    private toastService: ToastService
  ) {
    this.documentExpiryService = _documentExpiryService;
  }

  ngOnInit(): void {
    this.loadExpiryAlerts();

    // Debounce search query
    this.searchSubject
      .pipe(debounceTime(300), takeUntil(this.destroy$))
      .subscribe(() => {
        this.applyFilters();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadExpiryAlerts(): void {
    this.loading = true;
    this.error = null;

    // Load critical (7 days)
    this._documentExpiryService.getCriticalExpiringDocuments(0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ExpiryAlertResponse) => {
          this.criticalDocuments = response.content;
          if (response.expiryStats) {
            this.expiryStats = response.expiryStats;
          }
          this.loadUpcomingDocuments();
        },
        error: (err: any) => {
          console.error('Error loading critical documents:', err);
          this.loadUpcomingDocuments();
        }
      });
  }

  private loadUpcomingDocuments(): void {
    this._documentExpiryService.getUpcomingExpiringDocuments(0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ExpiryAlertResponse) => {
          this.upcomingDocuments = response.content;
          this.loadExpiredDocuments();
        },
        error: (err: any) => {
          console.error('Error loading upcoming documents:', err);
          this.loadExpiredDocuments();
        }
      });
  }

  private loadExpiredDocuments(): void {
    this._documentExpiryService.getExpiredDocuments(0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: ExpiryAlertResponse) => {
          this.expiredDocuments = response.content;
          this.loading = false;
          this.applyFilters();
        },
        error: (err: any) => {
          this.loading = false;
          this.error = 'Failed to load some expiry data';
        }
      });
  }

  applyFilters(): void {
    this.filteredCriticalDocuments = this.filterDocuments(this.criticalDocuments);
    this.filteredUpcomingDocuments = this.filterDocuments(this.upcomingDocuments);
    this.filteredExpiredDocuments = this.filterDocuments(this.expiredDocuments);
  }

  private filterDocuments(documents: LegalDocument[]): LegalDocument[] {
    return documents.filter((doc) => {
      // Search filter
      if (this.searchQuery.trim()) {
        const query = this.searchQuery.toLowerCase();
        const matchesSearch =
          doc.username?.toLowerCase().includes(query) ||
          doc.documentType?.name?.toLowerCase().includes(query) ||
          doc.fileHash?.toLowerCase().includes(query) ||
          doc.documentUrl?.toLowerCase().includes(query);

        if (!matchesSearch) return false;
      }

      // Document type filter
      if (this.selectedDocumentType) {
        if (doc.documentType?.name !== this.selectedDocumentType) return false;
      }

      // Status filter
      if (this.selectedStatus) {
        if (doc.status !== this.selectedStatus) return false;
      }

      return true;
    });
  }

  onSearchChange(event: any): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchQuery = query;
    this.searchSubject.next(query);
  }

  onDocumentTypeFilterChange(event: any): void {
    const documentType = (event.target as HTMLSelectElement).value;
    this.selectedDocumentType = documentType;
    this.applyFilters();
  }

  onStatusFilterChange(event: any): void {
    const status = (event.target as HTMLSelectElement).value;
    this.selectedStatus = status;
    this.applyFilters();
  }

  clearFilters(): void {
    this.searchQuery = '';
    this.selectedDocumentType = '';
    this.selectedStatus = '';
    this.applyFilters();
  }

  canDownloadDocument(document: LegalDocument): boolean {
    return document && !!document.documentUrl;
  }

  private getFullDocumentUrl(documentUrl: string): string {
    if (!documentUrl) return '';

    // Normalize Windows path separators to URL separators.
    let normalizedPath = documentUrl.replace(/\\/g, '/').trim();

    // If the backend sends a full URL, keep only the pathname for normalization.
    if (normalizedPath.startsWith('http://') || normalizedPath.startsWith('https://')) {
      try {
        normalizedPath = new URL(normalizedPath).pathname;
      } catch {
        normalizedPath = normalizedPath.replace(/^https?:\/\/[^/]+\/?/, '');
      }
    }

    // Remove any absolute prefix up to and including pidev-uploads/.
    if (normalizedPath.includes('pidev-uploads/')) {
      normalizedPath = normalizedPath.split('pidev-uploads/')[1];
    }

    // Ensure we don't keep leading slashes when appending to fileServerUrl.
    normalizedPath = normalizedPath.replace(/^\/+/, '');

    return `${this.fileServerUrl}${normalizedPath}`;
  }

  viewDocument(document: LegalDocument): void {
    if (!document || !document.id) return;

    // Try using documentUrl if available (faster fallback)
    if (document.documentUrl) {
      const fullUrl = this.getFullDocumentUrl(document.documentUrl);
      window.open(fullUrl, '_blank');
      this.toastService.success('Success', 'Document opened');
      return;
    }

    // Fallback to API download endpoint
    this.documentService
      .downloadDocument(document.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          window.open(url, '_blank');
          window.URL.revokeObjectURL(url);
          this.toastService.success('Success', 'Document opened');
        },
        error: (error) => {
          console.error('Error viewing document:', error);
          this.toastService.error(
            'Error',
            'Document not available. Please contact support.'
          );
        }
      });
  }

  downloadDocument(document: LegalDocument): void {
    if (!document || !document.id) return;

    // Try using documentUrl if available
    if (document.documentUrl) {
      const fullUrl = this.getFullDocumentUrl(document.documentUrl);
      const link = window.document.createElement('a');
      link.href = fullUrl;
      link.download = `document-${document.id}.pdf`;
      link.target = '_blank';
      link.click();
      this.toastService.success('Success', 'Document downloaded');
      return;
    }

    // Fallback to API download endpoint
    this.documentService
      .downloadDocument(document.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = window.document.createElement('a');
          link.href = url;
          link.download = `document-${document.id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.toastService.success('Success', 'Document downloaded');
        },
        error: (error) => {
          console.error('Error downloading document:', error);
          this.toastService.error(
            'Error',
            'Document not available. Please contact support.'
          );
        }
      });
  }

  private getFileExtension(mimeType: string): string {
    const mimeToExtension: Record<string, string> = {
      'application/pdf': '.pdf',
      'image/jpeg': '.jpg',
      'image/png': '.png',
      'image/gif': '.gif',
      'application/msword': '.doc',
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': '.docx',
      'application/vnd.ms-excel': '.xls',
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': '.xlsx',
      'text/plain': '.txt'
    };
    return mimeToExtension[mimeType] || '.pdf';
  }

  switchTab(tab: 'critical' | 'upcoming' | 'expired'): void {
    this.activeTab = tab;
  }

  getDaysUntilExpiry(expiryDate: string): number {
    return this.documentExpiryService.getDaysUntilExpiry(expiryDate);
  }

  getUrgencyLabel(expiryDate: string): string {
    return this.documentExpiryService.getUrgencyLabel(expiryDate);
  }

  getUrgencyColor(expiryDate: string): string {
    const level = this.documentExpiryService.getUrgencyLevel(expiryDate);
    const colors: Record<string, string> = {
      critical: '#dc3545',
      warning: '#f57f17',
      info: '#1a73e8'
    };
    return colors[level] || '#1a73e8';
  }

  getUrgencyIcon(expiryDate: string): string {
    const level = this.documentExpiryService.getUrgencyLevel(expiryDate);
    const icons: Record<string, string> = {
      critical: 'fas fa-exclamation-circle',
      warning: 'fas fa-exclamation-triangle',
      info: 'fas fa-info-circle'
    };
    return icons[level] || 'fas fa-info-circle';
  }

  refresh(): void {
    this.loadExpiryAlerts();
  }

  get currentDocuments(): LegalDocument[] {
    switch (this.activeTab) {
      case 'critical':
        return this.filteredCriticalDocuments;
      case 'upcoming':
        return this.filteredUpcomingDocuments;
      case 'expired':
        return this.filteredExpiredDocuments;
      default:
        return [];
    }
  }

  get tabCounts() {
    return {
      critical: this.filteredCriticalDocuments.length,
      upcoming: this.filteredUpcomingDocuments.length,
      expired: this.filteredExpiredDocuments.length
    };
  }

  getUniqueDocumentTypes(): string[] {
    const allDocs = [...this.criticalDocuments, ...this.upcomingDocuments, ...this.expiredDocuments];
    const types = new Set(allDocs.map((doc) => doc.documentType?.name).filter(Boolean));
    return Array.from(types) as string[];
  }

  getUniqueStatuses(): string[] {
    const allDocs = [...this.criticalDocuments, ...this.upcomingDocuments, ...this.expiredDocuments];
    const statuses = new Set(allDocs.map((doc) => doc.status));
    return Array.from(statuses);
  }
}
