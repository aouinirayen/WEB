import { Component, OnInit } from '@angular/core';
import { AuthService, User } from '../../services/auth/auth.service';
import { AdminUserService } from '../../services/admin/admin-user.service';
import { DocumentService } from '../../services/documents/document.service';
import { UserActivityService } from '../../services/admin/user-activity.service';
import { DocumentExpiryService } from '../../services/documents/document-expiry.service';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-admin-dhasbord',
  templateUrl: './admin-dhasbord.component.html',
  styleUrls: ['./admin-dhasbord.component.css']
})
export class AdminDhasbordComponent implements OnInit {
  currentUser$!: Observable<User | null>;
  currentUser: User | null = null;

  totalUsers: number = 0;
  pendingDocuments: number = 0;
  criticalExpiryAlerts: number = 0;
  todayAuditLogs: number = 0;
  loadingStats: boolean = true;

  constructor(
    private authService: AuthService,
    private adminUserService: AdminUserService,
    private documentService: DocumentService,
    private userActivityService: UserActivityService,
    private documentExpiryService: DocumentExpiryService
  ) {}

  ngOnInit(): void {
    this.currentUser$ = this.authService.currentUser$;
    this.currentUser = this.authService.currentUserValue;
    this.loadDashboardStats();
  }

  private loadDashboardStats(): void {
    this.loadingStats = true;

    // Fetch total users
    this.adminUserService.getAllUsers({ page: 0, size: 1 }).subscribe({
      next: (response) => {
        this.totalUsers = response.totalElements;
      },
      error: (error) => {
        console.error('Error fetching total users:', error);
        this.totalUsers = 0;
      }
    });

    // Fetch pending documents
    this.documentService.getPendingDocuments(0, 1).subscribe({
      next: (response) => {
        this.pendingDocuments = response.totalElements;
      },
      error: (error) => {
        console.error('Error fetching pending documents:', error);
        this.pendingDocuments = 0;
      }
    });

    // Fetch critical expiry alerts (documents expiring within 7 days)
    this.documentExpiryService.getCriticalExpiringDocuments(0, 1).subscribe({
      next: (response) => {
        this.criticalExpiryAlerts = response.totalElements;
      },
      error: (error) => {
        console.error('Error fetching critical expiry alerts:', error);
        this.criticalExpiryAlerts = 0;
      }
    });

    // Fetch today's audit logs - get all logs and filter by date on frontend
    this.userActivityService.searchActivityLogs({
      page: 0,
      size: 100
    }).subscribe({
      next: (response) => {
        // Filter logs from today on the frontend
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);

        const todayLogs = response.content.filter(log => {
          const logDate = new Date(log.timestamp);
          return logDate >= today && logDate < tomorrow;
        });

        this.todayAuditLogs = todayLogs.length;
        this.loadingStats = false;
      },
      error: (error) => {
        console.error('Error fetching today\'s audit logs:', error);
        this.todayAuditLogs = 0;
        this.loadingStats = false;
      }
    });
  }
}
