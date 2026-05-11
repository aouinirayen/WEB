import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { catchError, finalize, from, Observable, Subject, switchMap, takeUntil, throwError } from 'rxjs';
import { LegalDocument } from '../../../models/document.model';
import { BackendHealthResponse, DocumentSummaryResponse } from '../../../models/document-summary.model';
import { DocumentApiService } from '../../../services/documents/document-api.service';
import { DocumentService } from '../../../services/documents/document.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-text-generator',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './text-generator.component.html',
  styleUrls: ['./text-generator.component.css']
})
export class TextGeneratorComponent implements OnInit, OnChanges, OnDestroy {
  private readonly uploadsProxyBasePath = '/pidev-uploads/';

  @Input() document: LegalDocument | null = null;

  loading = false;
  healthState: 'checking' | 'healthy' | 'unhealthy' = 'checking';
  healthMessage = 'Checking backend connection...';
  errorMessage = '';
  copiedMessage = '';
  documentType = '';
  extractedText = '';
  summary = '';
  textLength = 0;
  backendMessage = '';

  private readonly destroy$ = new Subject<void>();

  constructor(
    private readonly documentApiService: DocumentApiService,
    private readonly documentService: DocumentService
  ) {}

  ngOnInit(): void {
    // Health check is optional and controlled by environment config
    if (environment.enableHealthCheck) {
      this.checkHealth();
    } else {
      // Default to checking state for UI consistency
      this.healthState = 'checking';
      this.healthMessage = 'Backend connection check disabled in development.';
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['document']) {
      this.resetState();
    }
  }

  summarizeDocument(): void {
    if (!this.document?.id) {
      this.errorMessage = 'Select a document before generating a summary.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.copiedMessage = '';
    this.backendMessage = '';
    this.documentType = '';
    this.extractedText = '';
    this.summary = '';
    this.textLength = 0;

    const currentDocument = this.document;

    this.getDocumentBlobForSummary(currentDocument)
      .pipe(
        switchMap(blob => {
          const fileName = this.getDocumentFileName(currentDocument);
          const file = new File([blob], fileName, { type: blob.type || 'application/octet-stream' });
          return this.documentApiService.summarize(file, true);
        }),
        takeUntil(this.destroy$),
        finalize(() => {
          this.loading = false;
        })
      )
      .subscribe({
        next: response => this.handleSummarySuccess(response),
        error: error => {
          this.errorMessage = error instanceof Error ? error.message : 'Failed to summarize this document.';
        }
      });
  }

  private getDocumentBlobForSummary(document: LegalDocument): Observable<Blob> {
    const candidateUrls = this.getDocumentFetchCandidates(document.documentUrl);

    if (candidateUrls.length > 0) {
      return from(this.fetchBlobFromCandidates(candidateUrls)).pipe(
        catchError(() => this.documentService.downloadDocument(document.id))
      );
    }

    return this.documentService.downloadDocument(document.id).pipe(
      catchError(() => throwError(() => new Error('Document file is unavailable for summarization.')))
    );
  }

  private getDocumentFetchCandidates(documentUrl: string): string[] {
    if (!documentUrl) {
      return [];
    }

    let normalizedPath = documentUrl.replace(/\\/g, '/').trim();

    if (normalizedPath.startsWith('http://') || normalizedPath.startsWith('https://')) {
      try {
        normalizedPath = new URL(normalizedPath).pathname;
      } catch {
        normalizedPath = normalizedPath.replace(/^https?:\/\/[^/]+\/?/, '');
      }
    }

    normalizedPath = normalizedPath.replace(/^\/+/, '');
    if (!normalizedPath) {
      return [];
    }

    const candidates: string[] = [];

    if (normalizedPath.includes('pidev-uploads/')) {
      const suffix = normalizedPath.split('pidev-uploads/')[1].replace(/^\/+/, '');
      if (suffix) {
        candidates.push(`/pidev-uploads/${suffix}`);
      }
    }

    if (normalizedPath.startsWith('uploads/')) {
      candidates.push(`/${normalizedPath}`);
    }

    if (normalizedPath.startsWith('pidev-uploads/')) {
      candidates.push(`/${normalizedPath}`);
    }

    candidates.push(`${this.uploadsProxyBasePath}${normalizedPath}`);

    // Deduplicate while preserving priority.
    return candidates.filter((url, index) => candidates.indexOf(url) === index);
  }

  private async fetchBlobFromCandidates(urls: string[]): Promise<Blob> {
    let lastStatus: number | null = null;

    for (const url of urls) {
      try {
        const response = await fetch(url, { credentials: 'include' });
        if (response.ok) {
          return response.blob();
        }

        lastStatus = response.status;
      } catch {
        // Continue trying the next candidate.
      }
    }

    const statusLabel = lastStatus ? ` (last status: ${lastStatus})` : '';
    throw new Error(`Unable to retrieve the document file for summarization${statusLabel}.`);
  }

  private async fetchBlobFromUrl(url: string): Promise<Blob> {
    const response = await fetch(url, { credentials: 'include' });
    if (!response.ok) {
      throw new Error('Unable to retrieve the document file for summarization.');
    }

    return response.blob();
  }

  retry(): void {
    this.summarizeDocument();
  }

  copyExtractedText(): void {
    if (!this.extractedText.trim()) {
      return;
    }

    this.copyToClipboard(this.extractedText, 'Extracted text copied.');
  }

  copySummary(): void {
    if (!this.summary.trim()) {
      return;
    }

    this.copyToClipboard(this.summary, 'Summary copied.');
  }

  private handleSummarySuccess(response: DocumentSummaryResponse): void {
    if (!response.success) {
      this.errorMessage = response.message || 'Summary generation failed.';
      return;
    }

    this.documentType = response.document_type || 'Unknown';
    this.extractedText = response.extracted_text || '';
    this.summary = response.summary || '';
    this.textLength = response.text_length || 0;
    this.backendMessage = response.message || '';

    if (!this.summary.trim()) {
      this.summary = 'Summary not generated for this file.';
    }
  }

  private checkHealth(): void {
    this.healthState = 'checking';
    this.healthMessage = 'Checking backend connection...';

    this.documentApiService.health()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: BackendHealthResponse) => {
          this.healthState = 'healthy';
          this.healthMessage = response.status || 'Backend is online.';
        },
        error: error => {
          this.healthState = 'unhealthy';
          this.healthMessage = error instanceof Error ? error.message : 'Backend is unavailable.';
        }
      });
  }

  private resetState(): void {
    this.errorMessage = '';
    this.copiedMessage = '';
    this.documentType = '';
    this.extractedText = '';
    this.summary = '';
    this.textLength = 0;
    this.backendMessage = '';
  }

  private getDocumentFileName(document: LegalDocument | null): string {
    if (!document?.documentUrl) {
      return `document-${document?.id || 'file'}`;
    }

    const rawName = document.documentUrl.split('/').pop() || '';
    return rawName || `document-${document.id}`;
  }

  private copyToClipboard(text: string, successMessage: string): void {
    if (navigator.clipboard?.writeText) {
      navigator.clipboard.writeText(text)
        .then(() => {
          this.copiedMessage = successMessage;
        })
        .catch(() => {
          this.copyWithFallback(text, successMessage);
        });
      return;
    }

    this.copyWithFallback(text, successMessage);
  }

  private copyWithFallback(text: string, successMessage: string): void {
    const textarea = document.createElement('textarea');
    textarea.value = text;
    textarea.setAttribute('readonly', 'true');
    textarea.style.position = 'absolute';
    textarea.style.left = '-9999px';
    document.body.appendChild(textarea);
    textarea.select();

    try {
      document.execCommand('copy');
      this.copiedMessage = successMessage;
    } finally {
      document.body.removeChild(textarea);
    }
  }

  get extractedPreview(): string {
    if (!this.extractedText.trim()) {
      return 'No extracted text returned yet.';
    }

    return this.extractedText.length > 500
      ? `${this.extractedText.slice(0, 500)}...`
      : this.extractedText;
  }

  get hasSummary(): boolean {
    return !!this.summary.trim() && this.summary !== 'Summary not generated for this file.';
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

