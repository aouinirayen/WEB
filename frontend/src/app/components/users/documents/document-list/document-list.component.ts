import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DocumentService } from '../../../../services/documents/document.service';
import { ToastService } from '../../../../services/shared/toast.service';
import { LegalDocument, DocumentStatusEnum, Page } from '../../../../models';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';

@Component({
  selector: 'app-document-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, PaginationComponent],
  templateUrl: './document-list.component.html',
  styleUrls: ['./document-list.component.css']
})
export class DocumentListComponent implements OnInit, OnDestroy {
  documents: LegalDocument[] = [];

  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;

  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;

  // Filters
  selectedStatus: DocumentStatusEnum | '' = '';
  searchFilter: string = ''; // Search by document type, filename, document ID
  statuses = [
    DocumentStatusEnum.PENDING,
    DocumentStatusEnum.VALID,
    DocumentStatusEnum.REJECTED,
    DocumentStatusEnum.EXPIRED,
    DocumentStatusEnum.REQUEST_UPDATE
  ];

  private destroy$ = new Subject<void>();

  constructor(
    public documentService: DocumentService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.loading$ = this.documentService.loading$;
    this.error$ = this.documentService.error$;
  }

  ngOnInit(): void {
    this.loadDocuments();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDocuments(): void {
    this.documentService
      .getUserDocuments(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: Page<LegalDocument>) => {
          this.documents = response.content;
          this.totalElements = response.totalElements;
          this.totalPages = response.totalPages;
          this.currentPage = response.pageNumber;
        },
        error: (error) => {
          console.error('Error loading documents:', error);
          this.toastService.error('Error', 'Failed to load documents');
        }
      });
  }

  /**
   * Filter documents based on search term and status filter
   * This is a client-side filter for instant search results
   */
  get getFilteredDocuments(): LegalDocument[] {
    let filtered = this.documents;

    // Apply status filter
    if (this.selectedStatus) {
      filtered = filtered.filter(doc => doc.status === this.selectedStatus);
    }

    // Apply search filter
    if (this.searchFilter.trim()) {
      const searchTerm = this.searchFilter.toLowerCase();
      filtered = filtered.filter(doc => {
        // Search by document type name
        const docTypeMatch = doc.documentType?.name?.toLowerCase().includes(searchTerm);
        // Search by document ID
        const docIdMatch = doc.id?.toString().includes(searchTerm);
        // Search by filename (if available in documentUrl)
        const filenameMatch = doc.documentUrl?.toLowerCase().includes(searchTerm);

        return docTypeMatch || docIdMatch || filenameMatch;
      });
    }

    return filtered;
  }

  onSearchChange(): void {
    // Client-side search, no need to reload documents
    // The search filter is applied via the getFilteredDocuments getter
  }

  applyFilters(): void {
    // Filters are now applied via the getFilteredDocuments getter
  }

  onStatusFilterChange(): void {
    this.currentPage = 0;
    this.applyFilters();
  }

  onPageChanged(pageNumber: number): void {
    this.currentPage = pageNumber;
    this.loadDocuments();
  }

  onPageSizeChanged(pageSize: number): void {
    this.pageSize = pageSize;
    this.currentPage = 0;
    this.loadDocuments();
  }

  viewDocument(id: number): void {
    this.router.navigate(['/documents', id]);
  }

  downloadDocument(document: LegalDocument): void {
    this.documentService
      .downloadDocument(document.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (blob: Blob) => {
          const url = window.URL.createObjectURL(blob);
          const link = window.document.createElement('a');
          link.href = url;
          link.download = `document-${this.documents.find(d => d.id)?.id}.pdf`;
          link.click();
          window.URL.revokeObjectURL(url);
          this.toastService.success('Success', 'Document downloaded successfully');
        },
        error: (error) => {
          console.error('Error downloading document:', error);
          this.toastService.error('Error', 'Failed to download document');
        }
      });
  }

  deleteDocument(document: LegalDocument): void {
    if (!confirm(`Are you sure you want to delete this ${document.status} document?`)) {
      return;
    }

    this.documentService
      .deleteDocument(document.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Success', 'Document deleted successfully');
          this.loadDocuments();
        },
        error: (error) => {
          console.error('Error deleting document:', error);
          this.toastService.error('Error', 'Failed to delete document. Only PENDING or REJECTED documents can be deleted.');
        }
      });
  }

  canDelete(status: DocumentStatusEnum): boolean {
    return status === DocumentStatusEnum.PENDING || status === DocumentStatusEnum.REJECTED;
  }

  canReupload(status: DocumentStatusEnum): boolean {
    return status === DocumentStatusEnum.REQUEST_UPDATE;
  }

  reuploadDocument(id: number): void {
    this.router.navigate(['/documents', id, 'reupload']);
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

  getStatusColor(status: DocumentStatusEnum): string {
    const colorMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: '#FFC107',
      [DocumentStatusEnum.VALID]: '#28A745',
      [DocumentStatusEnum.REJECTED]: '#DC3545',
      [DocumentStatusEnum.EXPIRED]: '#FF6B00',
      [DocumentStatusEnum.REQUEST_UPDATE]: '#007BFF'
    };
    return colorMap[status] || '#6C757D';
  }

  trackByDocument(index: number, doc: LegalDocument): any {
    return doc.id;
  }
}
