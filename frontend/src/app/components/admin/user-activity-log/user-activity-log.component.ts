import { Component, OnInit, Input, OnDestroy } from '@angular/core';
import { UserActivityService, AuditLog, AuditLogResponse } from '../../../services/admin/user-activity.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-user-activity-log',
  templateUrl: './user-activity-log.component.html',
  styleUrls: ['./user-activity-log.component.css']
})
export class UserActivityLogComponent implements OnInit, OnDestroy {
  @Input() userId?: number;
  @Input() showAllLogs: boolean = false; // Show all activity logs if true
  @Input() pageSize: number = 10;
  @Input() showPagination: boolean = true;
  @Input() maxItems: number = 10; // Limit displayed items

  activityLogs: AuditLog[] = [];
  loading = false;
  error: string | null = null;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  private destroy$ = new Subject<void>();

  constructor(private userActivityService: UserActivityService) {}

  ngOnInit(): void {
    if (this.showAllLogs || this.userId) {
      this.loadActivityLogs();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadActivityLogs(page: number = 0): void {
    if (!this.showAllLogs && !this.userId) return;

    this.loading = true;

    const request = this.showAllLogs
      ? this.userActivityService.getAllActivityLogs(page, this.pageSize)
      : this.userActivityService.getUserActivityLog(this.userId!, page, this.pageSize);

    request
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: AuditLogResponse) => {
          this.activityLogs = response.content;
          this.currentPage = response.number;
          this.totalPages = response.totalPages;
          this.totalElements = response.totalElements;
          this.loading = false;
          this.error = null;
        },
        error: (err: any) => {
          this.loading = false;
          this.error = 'Failed to load activity logs';
          console.error('Error loading activity logs:', err);
        }
      });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadActivityLogs(page);
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

  canGoPrevious(): boolean {
    return this.currentPage > 0;
  }

  canGoNext(): boolean {
    return this.currentPage < this.totalPages - 1;
  }
}
