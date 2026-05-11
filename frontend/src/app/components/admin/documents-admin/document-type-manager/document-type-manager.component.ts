import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { DocumentTypeService } from '../../../../services/documents/document-type.service';
import { ToastService } from '../../../../services/shared/toast.service';
import { DocumentType, DocumentTypeCreateRequest, RoleEnum, Page } from '../../../../models/document.model';
import { PaginationComponent } from '../../../shared/pagination/pagination.component';

@Component({
  selector: 'app-document-type-manager',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule, RouterModule, PaginationComponent],
  templateUrl: './document-type-manager.component.html',
  styleUrls: ['./document-type-manager.component.css']
})
export class DocumentTypeManagerComponent implements OnInit, OnDestroy {
  documentTypes: DocumentType[] = [];
  typeForm: FormGroup;
  searchKeyword: string = '';

  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;

  // Pagination
  currentPage: number = 0;
  pageSize: number = 10;
  totalElements: number = 0;
  totalPages: number = 0;

  // Modal state
  showTypeForm: boolean = false;
  isEditMode: boolean = false;
  selectedTypeForEdit: DocumentType | null = null;
  selectedRoles: string[] = [];

  // Delete confirmation modal state
  showDeleteConfirm: boolean = false;
  typeToDelete: number | null = null;

  // Available roles
  availableRoles = [
    RoleEnum.ADMIN,
    RoleEnum.AGENT,
    RoleEnum.OPERATOR,
    RoleEnum.PASSENGER
  ];

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    public documentTypeService: DocumentTypeService,
    private toastService: ToastService
  ) {
    this.loading$ = this.documentTypeService.loading$;
    this.error$ = this.documentTypeService.error$;
    this.typeForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', Validators.required],
      requiresExpiry: [false],
      allowedRoles: [[], Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadDocumentTypes();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDocumentTypes(): void {
    this.documentTypeService.getDocumentTypes(this.currentPage, this.pageSize)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page: Page<DocumentType>) => {
          this.documentTypes = page.content;
          this.totalElements = page.totalElements;
          this.totalPages = page.totalPages;
        },
        error: (err) => {
          this.toastService.error('Error', 'Failed to load document types');
        }
      });
  }

  searchDocumentTypes(): void {
    this.currentPage = 0;
    this.documentTypeService.searchDocumentTypes(this.searchKeyword)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page: Page<DocumentType>) => {
          this.documentTypes = page.content;
          this.totalElements = page.totalElements;
        },
        error: (err) => {
          this.toastService.error('Error', 'Search failed');
        }
      });
  }

  openCreateModal(): void {
    this.isEditMode = false;
    this.selectedTypeForEdit = null;
    this.typeForm.reset();
    this.selectedRoles = [];
    this.showTypeForm = true;
  }

  openEditModal(type: DocumentType): void {
    this.isEditMode = true;
    this.selectedTypeForEdit = type;
    this.typeForm.patchValue({
      name: type.name,
      description: type.description,
      requiresExpiry: type.requiresExpiry
    });
    // Parse comma-separated roles string into array
    this.selectedRoles = type.allowedRoles ? type.allowedRoles.split(',').map(r => r.trim()) : [];
    this.showTypeForm = true;
  }

  closeModal(): void {
    this.showTypeForm = false;
    this.typeForm.reset();
  }

  saveDocumentType(): void {
    // Validate form
    if (!this.typeForm.get('name')?.value || this.typeForm.get('name')?.value.trim().length < 3) {
      this.toastService.error('Validation', 'Name is required (minimum 3 characters)');
      return;
    }

    if (!this.typeForm.get('description')?.value || this.typeForm.get('description')?.value.trim() === '') {
      this.toastService.error('Validation', 'Description is required');
      return;
    }

    if (this.selectedRoles.length === 0) {
      this.toastService.error('Validation', 'Please select at least one role');
      return;
    }

    // Convert selectedRoles array to comma-separated string
    const rolesString = this.selectedRoles.join(',');

    const request: DocumentTypeCreateRequest = {
      name: this.typeForm.get('name')?.value.trim(),
      description: this.typeForm.get('description')?.value.trim(),
      requiresExpiry: this.typeForm.get('requiresExpiry')?.value || false,
      allowedRoles: rolesString
    };

    console.log('Sending document type request:', JSON.stringify(request, null, 2));

    const operation = this.isEditMode && this.selectedTypeForEdit?.id
      ? this.documentTypeService.updateDocumentType(this.selectedTypeForEdit.id, request)
      : this.documentTypeService.createDocumentType(request);

    operation.pipe(takeUntil(this.destroy$)).subscribe({
      next: () => {
        this.toastService.success('Success', `Document type ${this.isEditMode ? 'updated' : 'created'}`);
        this.closeModal();
        this.loadDocumentTypes();
      },
      error: (err) => {
        const errorMessage = err?.error?.message || err?.message || `Failed to ${this.isEditMode ? 'update' : 'create'} document type`;
        this.toastService.error('Error', errorMessage);
        console.error('Document type save error:', err);
      }
    });
  }

  deleteDocumentType(id: number): void {
    this.typeToDelete = id;
    this.showDeleteConfirm = true;
  }

  /**
   * Cancel delete confirmation
   */
  cancelDelete(): void {
    this.showDeleteConfirm = false;
    this.typeToDelete = null;
  }

  /**
   * Confirm and delete the document type
   */
  confirmDeleteType(): void {
    if (!this.typeToDelete) return;

    const typeId = this.typeToDelete;
    this.showDeleteConfirm = false;
    this.typeToDelete = null;

    this.documentTypeService.deleteDocumentType(typeId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.toastService.success('Success', 'Document type deleted');
          this.loadDocumentTypes();
        },
        error: (err) => {
          this.toastService.error('Error', 'Failed to delete document type');
        }
      });
  }

  toggleRole(role: string): void {
    const index = this.selectedRoles.indexOf(role);
    if (index > -1) {
      this.selectedRoles.splice(index, 1);
    } else {
      this.selectedRoles.push(role);
    }
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadDocumentTypes();
  }

  onPageSizeChange(size: number): void {
    this.pageSize = size;
    this.currentPage = 0;
    this.loadDocumentTypes();
  }

  trackByIndex(index: number): number {
    return index;
  }
}
