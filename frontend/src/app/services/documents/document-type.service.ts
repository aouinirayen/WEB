import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { DocumentType, DocumentTypeCreateRequest, Page } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class DocumentTypeService {
  private apiUrl = '/api/admin/document-types';
  private publicApiUrl = '/api/document-types'; // Public endpoint for document types

  // Shared state
  private documentTypesSubject = new BehaviorSubject<DocumentType[]>([]);
  public documentTypes$ = this.documentTypesSubject.asObservable();

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  private errorSubject = new BehaviorSubject<string | null>(null);
  public error$ = this.errorSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get all active document types with pagination
   */
  getDocumentTypes(page: number = 0, size: number = 10): Observable<Page<DocumentType>> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<DocumentType>>(this.publicApiUrl, { params }).pipe(
      tap(response => {
        this.documentTypesSubject.next(response.content);
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
   * Get all document types (public endpoint for users)
   */
  getPublicDocumentTypes(page: number = 0, size: number = 100): Observable<Page<DocumentType>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<DocumentType>>(this.publicApiUrl, { params }).pipe(
      tap(response => {
        this.documentTypesSubject.next(response.content);
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
   * Get document type by ID
   */
  getDocumentTypeById(id: number): Observable<DocumentType> {
    this.loadingSubject.next(true);
    return this.http.get<DocumentType>(`${this.apiUrl}/${id}`).pipe(
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
   * Search document types by keyword
   */
  searchDocumentTypes(keyword: string, page: number = 0, size: number = 10): Observable<Page<DocumentType>> {
    this.loadingSubject.next(true);
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<Page<DocumentType>>(`${this.apiUrl}/search`, { params }).pipe(
      tap(response => {
        this.documentTypesSubject.next(response.content);
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
   * Create new document type
   */
  createDocumentType(request: DocumentTypeCreateRequest): Observable<DocumentType> {
    this.loadingSubject.next(true);
    return this.http.post<DocumentType>(this.apiUrl, request).pipe(
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
   * Update document type
   */
  updateDocumentType(id: number, request: DocumentTypeCreateRequest): Observable<DocumentType> {
    this.loadingSubject.next(true);
    return this.http.put<DocumentType>(`${this.apiUrl}/${id}`, request).pipe(
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
   * Delete (soft delete) document type
   */
  deleteDocumentType(id: number): Observable<any> {
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
      return error.error.message || 'An error occurred';
    } else {
      return error.error?.message || error.error?.errors?.[0] || error.statusText || 'An error occurred';
    }
  }
}
