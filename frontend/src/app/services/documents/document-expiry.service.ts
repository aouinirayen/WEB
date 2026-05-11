import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { throwError } from 'rxjs';
import { LegalDocument, DocumentStatusEnum } from '../../models';

export interface ExpiryAlertResponse {
  content: LegalDocument[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  expiryStats?: {
    withinSevenDays: number;
    withinThirtyDays: number;
    withinNinetyDays: number;
    alreadyExpired: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class DocumentExpiryService {
  private readonly apiUrl = '/api/documents';

  private expiringDocumentsSubject = new BehaviorSubject<LegalDocument[]>([]);
  public expiringDocuments$ = this.expiringDocumentsSubject.asObservable();

  private expiryStatsSubject = new BehaviorSubject<any>(null);
  public expiryStats$ = this.expiryStatsSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get documents expiring within specified number of days
   * @param days - Number of days to check (e.g., 30 for documents expiring in next 30 days)
   * @param page - Pagination page
   * @param size - Page size
   */
  getExpiringDocuments(days: number = 30, page: number = 0, size: number = 10): Observable<ExpiryAlertResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('days', days.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ExpiryAlertResponse>(`${this.apiUrl}/expiring-soon`, { params }).pipe(
      tap(response => {
        this.expiringDocumentsSubject.next(response.content);
        if (response.expiryStats) {
          this.expiryStatsSubject.next(response.expiryStats);
        }
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
   * Get documents expiring within 7 days (critical)
   */
  getCriticalExpiringDocuments(page: number = 0, size: number = 20): Observable<ExpiryAlertResponse> {
    return this.getExpiringDocuments(7, page, size);
  }

  /**
   * Get documents expiring within 30 days
   */
  getUpcomingExpiringDocuments(page: number = 0, size: number = 20): Observable<ExpiryAlertResponse> {
    return this.getExpiringDocuments(30, page, size);
  }

  /**
   * Get expired documents
   */
  getExpiredDocuments(page: number = 0, size: number = 20): Observable<ExpiryAlertResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ExpiryAlertResponse>(`${this.apiUrl}/expired`, { params }).pipe(
      tap(response => {
        this.expiringDocumentsSubject.next(response.content);
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
   * Get expiry alerts for specific user
   */
  getUserExpiryAlerts(userId: number, days: number = 30, page: number = 0, size: number = 10): Observable<ExpiryAlertResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('days', days.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ExpiryAlertResponse>(`${this.apiUrl}/expiring-soon/by-user`, { params }).pipe(
      tap(response => {
        this.expiringDocumentsSubject.next(response.content);
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
   * Get expiry alerts for specific document type
   */
  getDocumentTypeExpiryAlerts(documentTypeId: number, days: number = 30, page: number = 0, size: number = 20): Observable<ExpiryAlertResponse> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('documentTypeId', documentTypeId.toString())
      .set('days', days.toString())
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ExpiryAlertResponse>(`${this.apiUrl}/expiring-soon/by-type`, { params }).pipe(
      tap(response => {
        this.expiringDocumentsSubject.next(response.content);
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
   * Get expiry statistics summary
   */
  getExpiryStatistics(): Observable<any> {
    this.loadingSubject.next(true);
    return this.http.get<any>(`${this.apiUrl}/expiry-statistics`).pipe(
      tap(response => {
        this.expiryStatsSubject.next(response);
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
   * Calculate days until expiry
   */
  getDaysUntilExpiry(expiryDate: string): number {
    const expiry = new Date(expiryDate);
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    expiry.setHours(0, 0, 0, 0);

    const diffTime = expiry.getTime() - today.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    return diffDays;
  }

  /**
   * Get urgency level based on days until expiry
   */
  getUrgencyLevel(expiryDate: string): 'critical' | 'warning' | 'info' {
    const daysLeft = this.getDaysUntilExpiry(expiryDate);

    if (daysLeft <= 0) return 'critical'; // Already expired
    if (daysLeft <= 7) return 'critical';
    if (daysLeft <= 30) return 'warning';
    return 'info';
  }

  /**
   * Get urgency color class
   */
  getUrgencyColorClass(expiryDate: string): string {
    const urgency = this.getUrgencyLevel(expiryDate);
    const colors: Record<string, string> = {
      critical: 'alert-critical',
      warning: 'alert-warning',
      info: 'alert-info'
    };
    return colors[urgency] || 'alert-info';
  }

  /**
   * Get urgency label
   */
  getUrgencyLabel(expiryDate: string): string {
    const daysLeft = this.getDaysUntilExpiry(expiryDate);

    if (daysLeft < 0) return 'EXPIRED';
    if (daysLeft === 0) return 'EXPIRES TODAY';
    if (daysLeft === 1) return 'EXPIRES TOMORROW';
    if (daysLeft <= 7) return `EXPIRES IN ${daysLeft} DAYS`;
    if (daysLeft <= 30) return `EXPIRES IN ${daysLeft} DAYS`;
    return `EXPIRES IN ${daysLeft} DAYS`;
  }

  private getErrorMessage(error: any): string {
    if (error.error?.message) {
      return error.error.message;
    }
    return 'An error occurred while fetching document expiry information';
  }
}
