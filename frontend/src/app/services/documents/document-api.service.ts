import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError, timeout } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiError, BackendHealthResponse, DocumentSummaryResponse } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class DocumentApiService {
  private readonly apiBaseUrl = environment.documentSummaryApiBaseUrl ?? environment.apiBaseUrl;
  private readonly healthUrl = environment.healthUrl;
  private readonly healthTimeoutMs = 10000;
  private readonly summarizeTimeoutMs = 180000;

  constructor(private http: HttpClient) {}

  health(): Observable<BackendHealthResponse> {
    return this.http.get<BackendHealthResponse>(this.healthUrl).pipe(
      timeout(this.healthTimeoutMs),
      catchError(error => this.handleError(error))
    );
  }

  summarize(file: File, generateSummary: boolean = true): Observable<DocumentSummaryResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('generate_summary', generateSummary ? 'true' : 'false');

    return this.http.post<DocumentSummaryResponse>(`${this.apiBaseUrl}/summarize`, formData).pipe(
      timeout(this.summarizeTimeoutMs),
      map(response => this.normalizeSummaryResponse(response)),
      catchError(error => this.handleError(error))
    );
  }

  private normalizeSummaryResponse(response: DocumentSummaryResponse): DocumentSummaryResponse {
    return {
      success: response.success,
      document_type: response.document_type || 'Unknown',
      extracted_text: response.extracted_text || '',
      summary: response.summary || '',
      text_length: Number(response.text_length || 0),
      message: response.message || ''
    };
  }

  private handleError(error: unknown): Observable<never> {
    const mappedMessage = this.mapErrorMessage(error);
    return throwError(() => new Error(mappedMessage));
  }

  private mapErrorMessage(error: unknown): string {
    if (error instanceof Error && error.name === 'TimeoutError') {
      return 'The request timed out. Large documents may need more time to process.';
    }

    if (error instanceof HttpErrorResponse) {
      const apiError = error.error as ApiError | string | null | undefined;

      if (typeof apiError === 'string' && apiError.trim()) {
        return apiError;
      }

      if (apiError && typeof apiError === 'object' && 'error' in apiError && typeof apiError.error === 'string' && apiError.error.trim()) {
        return apiError.error;
      }

      if (error.status === 0) {
        return 'Cannot reach the backend. Make sure Flask is running on http://127.0.0.1:5000.';
      }

      return error.message || error.statusText || 'The backend request failed.';
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return 'An unexpected error occurred while contacting the backend.';
  }
}
