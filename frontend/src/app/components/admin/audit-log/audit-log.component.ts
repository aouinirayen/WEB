import { Component, OnInit, OnDestroy } from '@angular/core';
import { UserActivityService, AuditLog, AuditLogResponse, ActivityActionType } from '../../../services/admin/user-activity.service';
import { Subject } from 'rxjs';
import { takeUntil, debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { FormBuilder, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-audit-log',
  templateUrl: './audit-log.component.html',
  styleUrls: ['./audit-log.component.css']
})
export class AuditLogComponent implements OnInit, OnDestroy {
  auditLogs: AuditLog[] = [];
  loading = false;
  error: string | null = null;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  pageSize = 10;

  filterForm!: FormGroup;
  actionTypes: ActivityActionType[] = [
    'LOGIN',
    'LOGOUT',
    'DOCUMENT_UPLOADED',
    'DOCUMENT_VIEWED',
    'DOCUMENT_DOWNLOADED',
    'DOCUMENT_APPROVED',
    'DOCUMENT_REJECTED',
    'USER_CREATED',
    'USER_UPDATED',
    'USER_DELETED',
    'PROFILE_UPDATED',
    'PASSWORD_CHANGED'
  ];

  private destroy$ = new Subject<void>();
  private searchSubject$ = new Subject<string>();

  constructor(
    private userActivityService: UserActivityService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.setupSearchListener();
    this.loadAuditLogs();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  initializeForm(): void {
    // Set default date range: today - 7 days to today
    const today = new Date();
    const sevenDaysAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000);

    this.filterForm = this.formBuilder.group({
      username: [''],
      actionType: [''],
      dateFrom: [this.formatDateForInput(sevenDaysAgo)],
      dateTo: [this.formatDateForInput(today)]
    });
  }

  private formatDateForInput(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  setupSearchListener(): void {
    this.searchSubject$
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.currentPage = 0;
        this.loadAuditLogs();
      });
  }

  onSearchChange(username: string): void {
    this.searchSubject$.next(username);
  }

  onActionTypeChange(): void {
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  onDateChange(): void {
    const formValue = this.filterForm.value;
    const dateFrom = new Date(formValue.dateFrom);
    const dateTo = new Date(formValue.dateTo);

    // Validate date range (max 7 days)
    const diffTime = dateTo.getTime() - dateFrom.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays > 7) {
      this.error = 'Date range cannot exceed 7 days';
      return;
    }

    this.error = null;
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  loadAuditLogs(page: number = 0): void {
    this.loading = true;
    const formValue = this.filterForm.value;

    console.log('=== LOADING AUDIT LOGS ===');
    console.log('Filters:', {
      page,
      pageSize: this.pageSize,
      username: formValue.username || 'none',
      actionType: formValue.actionType || 'none',
      dateFrom: formValue.dateFrom || 'none',
      dateTo: formValue.dateTo || 'none'
    });

    // Build search filters
    const filters: any = {
      page,
      size: this.pageSize,
      ...(formValue.actionType && { actionType: formValue.actionType })
    };

    // Add date filters if provided
    if (formValue.dateFrom) {
      filters.dateFrom = `${formValue.dateFrom}T00:00:00`;
    }
    if (formValue.dateTo) {
      filters.dateTo = `${formValue.dateTo}T23:59:59`;
    }

    this.userActivityService.searchActivityLogs(filters)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: AuditLogResponse) => {
          console.log('=== BACKEND RESPONSE ===');
          console.log('Total Elements:', response.totalElements);
          console.log('Total Pages:', response.totalPages);
          console.log('Current Page:', response.number);
          console.log('Page Size:', response.size);
          console.log('Audit Logs:', response.content);

          // Filter by username in frontend if provided
          let filteredLogs = response.content;
          if (formValue.username) {
            const searchTerm = formValue.username.toLowerCase();
            filteredLogs = filteredLogs.filter(log =>
              log.username.toLowerCase().includes(searchTerm)
            );
          }

          // Exclude admin activity logs
          filteredLogs = filteredLogs.filter(log =>
            !log.username.toLowerCase().includes('admin')
          );

          // Sort by timestamp descending (latest first)
          filteredLogs.sort((a, b) => {
            const dateA = new Date(a.timestamp).getTime();
            const dateB = new Date(b.timestamp).getTime();
            return dateB - dateA;
          });

          this.auditLogs = filteredLogs;
          this.currentPage = response.number;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
          this.error = null;

          console.log('=== FILTERED & DISPLAYED LOGS ===');
          console.log('Displayed (sorted by timestamp desc):', this.auditLogs);
        },
        error: (err: any) => {
          console.error('=== ERROR LOADING AUDIT LOGS ===');
          console.error('Error Details:', err);

          this.loading = false;
          this.error = 'Failed to load audit logs';
        }
      });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadAuditLogs(page);
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.goToPage(this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.goToPage(this.currentPage - 1);
    }
  }

  canGoPrevious(): boolean {
    return this.currentPage > 0;
  }

  canGoNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }

  resetFilters(): void {
    this.initializeForm();
    this.currentPage = 0;
    this.error = null;
    this.loadAuditLogs();
  }

  getActionLabel(actionType: string): string {
    return this.userActivityService.getActionLabel(actionType as any);
  }

  getActionIcon(actionType: string): string {
    return this.userActivityService.getActionIcon(actionType as any);
  }

  getStatusClass(status: string): string {
    return status === 'SUCCESS' ? 'status-success' : 'status-failed';
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleString();
  }

  // Hide API call descriptions like "GET /api/documents"
  shouldHideDescription(description: string): boolean {
    return /^(GET|POST|PUT|DELETE|PATCH)\s+\/api\//.test(description);
  }

  // Map IP address to location (localhost mapping)
  getLocationFromIP(ipAddress: string): string {
    // Map localhost addresses
    if (ipAddress === '0:0:0:0:0:0:0:1' || ipAddress === '::1' || ipAddress === '127.0.0.1') {
      return 'Localhost';
    }
    // For other IPs, just return the IP for now (real geolocation would require an API)
    return ipAddress;
  }
}
