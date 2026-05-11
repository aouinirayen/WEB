import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, tap, map } from 'rxjs/operators';
import {
  LegalDocument,
  Page,
  DocumentSearchCriteria,
  DocumentUploadRequest,
  DocumentStatusEnum,
  ErrorResponse
} from '../../models';

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private apiUrl = '/api/documents';
  private adminApiUrl = '/api/admin/documents';

  // Shared state for documents list
  private userDocumentsSubject = new BehaviorSubject<LegalDocument[]>([]);
  public userDocuments$ = this.userDocumentsSubject.asObservable();

  private adminDocumentsSubject = new BehaviorSubject<LegalDocument[]>([]);
  public adminDocuments$ = this.adminDocumentsSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get user's documents with pagination
   */
  getUserDocuments(page: number = 0, size: number = 10): Observable<Page<LegalDocument>> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<LegalDocument>>(`${this.apiUrl}`, { params }).pipe(
      tap(response => {
        this.userDocumentsSubject.next(response.content);
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
   * Get document details by ID
   */
  getDocumentById(id: number): Observable<LegalDocument> {
    this.loadingSubject.next(true);
    return this.http.get<LegalDocument>(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
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
   * Upload new document with multipart form data
   */
  uploadDocument(documentTypeId: number, file: File, expiryDate?: string): Observable<LegalDocument> {
    this.loadingSubject.next(true);
    const formData = new FormData();
    formData.append('file', file);
    formData.append('documentTypeId', documentTypeId.toString());

    if (expiryDate) {
      // Convert date string (yyyy-MM-dd) to LocalDateTime format (yyyy-MM-ddTHH:mm:ss)
      const formattedDate = expiryDate.includes('T') ? expiryDate : `${expiryDate}T00:00:00`;
      formData.append('expiryDate', formattedDate);
    }

    return this.http.post<LegalDocument>(`${this.apiUrl}`, formData).pipe(
      tap(response => {
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
   * Download document file
   */
  downloadDocument(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/download`, { responseType: 'blob' }).pipe(
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Delete document (only PENDING or REJECTED status)
   */
  deleteDocument(id: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Re-upload document after REQUEST_UPDATE status
   */
  reuploadDocument(id: number, file: File, customFields?: Record<string, any>): Observable<LegalDocument> {
    const formData = new FormData();
    formData.append('file', file);

    if (customFields) {
      formData.append('customFields', JSON.stringify(customFields));
    }

    return this.http.post<LegalDocument>(`${this.apiUrl}/${id}/reupload`, formData).pipe(
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Search all documents with filters
   */
  searchDocuments(criteria: DocumentSearchCriteria): Observable<Page<LegalDocument>> {
    this.loadingSubject.next(true);
    let params = new HttpParams()
      .set('page', criteria.page.toString())
      .set('size', criteria.size.toString());

    if (criteria.userId) {
      params = params.set('userId', criteria.userId.toString());
    }
    if (criteria.documentTypeId) {
      params = params.set('documentTypeId', criteria.documentTypeId.toString());
    }
    if (criteria.status) {
      params = params.set('status', criteria.status);
    }

    return this.http.get<Page<LegalDocument>>(`${this.adminApiUrl}`, { params }).pipe(
      tap(response => {
        this.adminDocumentsSubject.next(response.content);
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
   * ADMIN: Get pending documents for review
   */
  getPendingDocuments(page: number = 0, size: number = 10): Observable<Page<LegalDocument>> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<LegalDocument>>(`${this.adminApiUrl}/pending`, { params }).pipe(
      tap(response => {
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
   * ADMIN: Get document details by ID
   */
  getAdminDocumentById(id: number): Observable<LegalDocument> {
    this.loadingSubject.next(true);
    return this.http.get<LegalDocument>(`${this.adminApiUrl}/${id}`).pipe(
      tap(() => {
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
   * ADMIN: Get all documents of a user
   */
  getUserDocumentsByUserId(userId: number, page: number = 0, size: number = 10): Observable<Page<LegalDocument>> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<LegalDocument>>(`${this.adminApiUrl}/user/${userId}`, { params }).pipe(
      tap(response => {
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
   * ADMIN: Approve document
   */
  approveDocument(id: number): Observable<LegalDocument> {
    return this.http.post<LegalDocument>(`${this.adminApiUrl}/${id}/approve`, {}).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Reject document with reason (not stored in DB)
   */
  rejectDocument(id: number, reason?: string): Observable<LegalDocument> {
    const body: any = { status: 'REJECTED' };
    if (reason) {
      body.rejectionReason = reason;
    }
    return this.http.post<LegalDocument>(`${this.adminApiUrl}/${id}/reject`, body).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Request document update with feedback (not stored in DB)
   */
  requestDocumentUpdate(id: number, feedback: string): Observable<LegalDocument> {
    return this.http.post<LegalDocument>(`${this.adminApiUrl}/${id}/request-update`, { rejectionReason: feedback }).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Force delete document
   */
  forceDeleteDocument(id: number): Observable<any> {
    return this.http.delete(`${this.adminApiUrl}/${id}`).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Manually trigger expiry check
   */
  checkExpiredDocuments(): Observable<any> {
    return this.http.post(`${this.adminApiUrl}/check-expired`, {}).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Manually trigger notifications
   */
  sendNotifications(): Observable<any> {
    return this.http.post(`${this.adminApiUrl}/send-notifications`, {}).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * ADMIN: Toggle document status between VALID and REJECTED
   * Only works with VALID and REJECTED documents
   */
  toggleDocumentStatus(documentId: number): Observable<LegalDocument> {
    return this.http.post<LegalDocument>(`${this.adminApiUrl}/${documentId}/toggle-status`, {}).pipe(
      tap(() => {
        this.errorSubject.next(null);
      }),
      catchError(error => {
        const errorMsg = this.getErrorMessage(error);
        this.errorSubject.next(errorMsg);
        return throwError(() => error);
      })
    );
  }

  /**
   * Clear error message
   */
  clearError(): void {
    this.errorSubject.next(null);
  }

  /**
   * Helper method to extract error message
   */
  private getErrorMessage(error: any): string {
    if (error.error instanceof ErrorEvent) {
      // Client-side error
      return error.error.message || 'An error occurred';
    } else {
      // Server-side error
      return error.error?.message || error.error?.errors?.[0] || error.statusText || 'An error occurred';
    }
  }
}
