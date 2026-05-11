import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { throwError } from 'rxjs';

export type ActivityActionType =
  | 'LOGIN'
  | 'LOGOUT'
  | 'DOCUMENT_UPLOADED'
  | 'DOCUMENT_VIEWED'
  | 'DOCUMENT_DOWNLOADED'
  | 'DOCUMENT_APPROVED'
  | 'DOCUMENT_REJECTED'
  | 'USER_CREATED'
  | 'USER_UPDATED'
  | 'USER_DELETED'
  | 'PROFILE_UPDATED'
  | 'PASSWORD_CHANGED';

export interface AuditLog {
  id: number;
  userId: number;
  username: string;
  actionType: ActivityActionType;
  resourceType: string; // 'DOCUMENT', 'USER', 'PROFILE', etc.
  resourceId?: number;
  resourceName?: string;
  description: string;
  ipAddress: string;
  userAgent?: string;
  timestamp: string; // ISO DateTime
  status: 'SUCCESS' | 'FAILED';
  errorDetails?: string;
}

export interface AuditLogResponse {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserActivityService {
  private readonly apiUrl = '/api/admin/users';

  private activityLogsSubject = new BehaviorSubject<AuditLog[]>([]);
  public activityLogs$ = this.activityLogsSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get activity log for a specific user with pagination
   */
  getUserActivityLog(userId: number, page: number = 0, size: number = 10): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/${userId}/activity-log`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get activity logs filtered by action type
   */
  getUserActivityLogByAction(
    userId: number,
    actionType: ActivityActionType,
    page: number = 0,
    size: number = 10
  ): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('actionType', actionType);

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/${userId}/activity-log/filter`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get all system activity logs (admin only)
   */
  getAllActivityLogs(page: number = 0, size: number = 20): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/activity-logs/all`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Search activity logs with filters
   */
  searchActivityLogs(
    filters: {
      userId?: number;
      actionType?: ActivityActionType;
      resourceType?: string;
      dateFrom?: string;
      dateTo?: string;
      page?: number;
      size?: number;
      sort?: string;
    }
  ): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    let params = new HttpParams();

    if (filters.userId) params = params.set('userId', filters.userId.toString());
    if (filters.actionType) params = params.set('actionType', filters.actionType);
    if (filters.resourceType) params = params.set('resourceType', filters.resourceType);
    if (filters.dateFrom) params = params.set('dateFrom', filters.dateFrom);
    if (filters.dateTo) params = params.set('dateTo', filters.dateTo);
    if (filters.sort) params = params.set('sort', filters.sort);
    params = params.set('page', (filters.page || 0).toString());
    params = params.set('size', (filters.size || 20).toString());

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/activity-logs/search`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get activity logs filtered by action type (system-wide)
   */
  getActivityLogsByAction(
    actionType: ActivityActionType,
    page: number = 0,
    size: number = 20
  ): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('actionType', actionType)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/activity-logs/by-action`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get activity logs filtered by resource type and ID
   */
  getActivityLogsByResource(
    resourceType: string,
    resourceId: number,
    page: number = 0,
    size: number = 20
  ): Observable<AuditLogResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('resourceType', resourceType)
      .set('resourceId', resourceId.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<AuditLogResponse>(`${this.apiUrl}/activity-logs/by-resource`, { params }).pipe(
      tap(response => {
        this.activityLogsSubject.next(response.content);
        this.loadingSubject.next(false);
        this.errorSubject.next(null);
      }),
      catchError(error => {
        this.loadingSubject.next(false);
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Get action type display label
   */
  getActionLabel(action: ActivityActionType): string {
    const labels: Record<ActivityActionType, string> = {
      LOGIN: 'Login',
      LOGOUT: 'Logout',
      DOCUMENT_UPLOADED: 'Document Uploaded',
      DOCUMENT_VIEWED: 'Document Viewed',
      DOCUMENT_DOWNLOADED: 'Document Downloaded',
      DOCUMENT_APPROVED: 'Document Approved',
      DOCUMENT_REJECTED: 'Document Rejected',
      USER_CREATED: 'User Created',
      USER_UPDATED: 'User Updated',
      USER_DELETED: 'User Deleted',
      PROFILE_UPDATED: 'Profile Updated',
      PASSWORD_CHANGED: 'Password Changed'
    };
    return labels[action] || action;
  }

  /**
   * Get action icon class
   */
  getActionIcon(action: ActivityActionType): string {
    const icons: Record<ActivityActionType, string> = {
      LOGIN: 'fas fa-sign-in-alt',
      LOGOUT: 'fas fa-sign-out-alt',
      DOCUMENT_UPLOADED: 'fas fa-upload',
      DOCUMENT_VIEWED: 'fas fa-eye',
      DOCUMENT_DOWNLOADED: 'fas fa-download',
      DOCUMENT_APPROVED: 'fas fa-check-circle',
      DOCUMENT_REJECTED: 'fas fa-times-circle',
      USER_CREATED: 'fas fa-user-plus',
      USER_UPDATED: 'fas fa-user-edit',
      USER_DELETED: 'fas fa-user-minus',
      PROFILE_UPDATED: 'fas fa-id-card',
      PASSWORD_CHANGED: 'fas fa-key'
    };
    return icons[action] || 'fas fa-circle';
  }

  private getErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }
    return 'An error occurred while fetching activity logs';
  }
}
