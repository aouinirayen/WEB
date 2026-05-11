import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DocumentService } from '../../../../services/documents/document.service';
import { ToastService } from '../../../../services/shared/toast.service';
import { LegalDocument, DocumentStatusEnum } from '../../../../models';
import { StatusBadgeComponent } from '../../../shared/status-badge/status-badge.component';

@Component({
  selector: 'app-document-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './document-detail.component.html',
  styleUrls: ['./document-detail.component.css']
})
export class DocumentDetailComponent implements OnInit, OnDestroy {
  document: LegalDocument | null = null;
  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public documentService: DocumentService,
    private toastService: ToastService
  ) {
    this.loading$ = this.documentService.loading$;
    this.error$ = this.documentService.error$;
  }

  ngOnInit(): void {
    this.route.params.pipe(takeUntil(this.destroy$)).subscribe(params => {
      const id = params['id'];
      if (id) {
        this.loadDocument(parseInt(id));
      }
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDocument(id: number): void {
    this.documentService
      .getDocumentById(id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (doc: LegalDocument) => {
          this.document = doc;
        },
        error: (error) => {
          console.error('Error loading document:', error);
          this.toastService.error('Error', 'Failed to load document');
        }
      });
  }

  downloadDocument(): void {
    if (!this.document) return;

    this.documentService
      .downloadDocument(this.document.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = window.document.createElement('a');
          link.href = url;
          link.download = `document-${this.document?.id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.toastService.success('Successs', 'Document downloaded');
        },
        error: (error) => {
          console.error('Error downloading document:', error);
          this.toastService.error('Error', 'Failed to download document');
        }
      });
  }

  reupload(): void {
    if (this.document) {
      this.router.navigate(['/documents', this.document.id, 'reupload']);
    }
  }

  goBack(): void {
    this.router.navigate(['/documents']);
  }

  getStatusLabel(status: DocumentStatusEnum): string {
    const statusMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: 'Pending Review',
      [DocumentStatusEnum.VALID]: 'Approved',
      [DocumentStatusEnum.REJECTED]: 'Rejected',
      [DocumentStatusEnum.EXPIRED]: 'Expired',
      [DocumentStatusEnum.REQUEST_UPDATE]: 'Update Requested'
    };
    return statusMap[status] || status;
  }

  canReupload(): boolean {
    return this.document?.status === DocumentStatusEnum.REQUEST_UPDATE;
  }

  isRejectedOrUpdateRequested(): boolean {
    return (
      this.document?.status === DocumentStatusEnum.REJECTED ||
      this.document?.status === DocumentStatusEnum.REQUEST_UPDATE
    );
  }

  getAllowedRoles(): string[] {
    if (!this.document?.documentType?.allowedRoles) return [];
    return this.document.documentType.allowedRoles.split(',').map(role => role.trim());
  }
}
