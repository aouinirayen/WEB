import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { DocumentService } from '../../../../services/documents/document.service';
import { DocumentTypeService } from '../../../../services/documents/document-type.service';
import { ToastService } from '../../../../services/shared/toast.service';
import { DocumentType, LegalDocument } from '../../../../models';

@Component({
  selector: 'app-document-upload',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './document-upload.component.html',
  styleUrls: ['./document-upload.component.css']
})
export class DocumentUploadComponent implements OnInit, OnDestroy {
  uploadForm: FormGroup;
  documentTypes: DocumentType[] = [];
  selectedFile: File | null = null;
  filePreview: string | ArrayBuffer | null = null;
  uploadProgress: number = 0;
  uploadInProgress: boolean = false;

  loading$!: Observable<boolean>;
  error$!: Observable<string | null>;

  private destroy$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private documentService: DocumentService,
    private documentTypeService: DocumentTypeService,
    private toastService: ToastService,
    private router: Router
  ) {
    this.loading$ = this.documentTypeService.loading$;
    this.error$ = this.documentTypeService.error$;
    this.uploadForm = this.fb.group({
      documentTypeId: ['', Validators.required],
      expiryDate: ['']
    });
  }

  ngOnInit(): void {
    this.loadDocumentTypes();

    // Subscribe to documentTypes$ from service
    this.documentTypeService.documentTypes$
      .pipe(takeUntil(this.destroy$))
      .subscribe(types => {
        this.documentTypes = types;
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadDocumentTypes(): void {
    this.documentTypeService
      .getDocumentTypes(0, 100)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (page) => {
          // Document types loaded successfully
        },
        error: (error) => {
          console.error('Error loading document types:', error);
          this.toastService.error('Error', 'Failed to load document types: ' + (error?.message || 'Unknown error'));
        }
      });
  }

  isExpiryRequired(): boolean {
    const documentTypeId = this.uploadForm.get('documentTypeId')?.value;
    if (documentTypeId) {
      const selectedType = this.documentTypes.find(dt => dt.id === parseInt(documentTypeId));
      return selectedType?.requiresExpiry || false;
    }
    return false;
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];

    if (file) {
      this.handleFileSelection(file);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    const dropZone = event.currentTarget as HTMLElement;
    dropZone.classList.add('drag-over');
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    const dropZone = event.currentTarget as HTMLElement;
    dropZone.classList.remove('drag-over');
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    const dropZone = event.currentTarget as HTMLElement;
    dropZone.classList.remove('drag-over');

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.handleFileSelection(files[0]);
    }
  }

  private handleFileSelection(file: File): void {
    console.log('🟡 handleFileSelection called with file:', file);

    // Validate file type and size
    const maxSize = 10 * 1024 * 1024; // 10MB
    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/png', 'image/jpg'];

    console.log('🟡 File size:', file.size, 'Max:', maxSize);
    console.log('🟡 File type:', file.type, 'Allowed:', allowedTypes);

    if (file.size > maxSize) {
      console.error('❌ File too large');
      this.toastService.error('Error', 'File size exceeds 10MB limit');
      return;
    }

    if (!allowedTypes.includes(file.type)) {
      console.error('❌ File type not allowed:', file.type);
      this.toastService.error('Error', 'Only PDF and image files are allowed');
      return;
    }

    this.selectedFile = file;
    console.log('✅ File selected:', this.selectedFile);

    // Generate file preview for images
    if (file.type.startsWith('image/')) {
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.filePreview = e.target.result;
        console.log('✅ Image preview generated');
      };
      reader.readAsDataURL(file);
    } else {
      this.filePreview = null;
      console.log('✅ PDF file selected (no preview)');
    }

    this.uploadForm.patchValue({ file: file.name });
    console.log('✅ Form updated with file name:', file.name);
  }

  removeFile(): void {
    this.selectedFile = null;
    this.filePreview = null;
    this.uploadForm.patchValue({ file: '' });
  }

  onSubmit(): void {
    console.log('🟡 onSubmit called');
    console.log('🟡 Form valid:', this.uploadForm.valid);
    console.log('🟡 Form value:', this.uploadForm.value);
    console.log('🟡 Selected file:', this.selectedFile);
    console.log('🟡 DocumentTypeId value:', this.uploadForm.get('documentTypeId')?.value);

    if (!this.uploadForm.valid || !this.selectedFile) {
      console.error('❌ Form validation failed');
      console.error('❌ Form valid:', this.uploadForm.valid);
      console.error('❌ Has file:', !!this.selectedFile);
      if (this.uploadForm.invalid) {
        console.error('❌ Form errors:', this.uploadForm.errors);
        Object.keys(this.uploadForm.controls).forEach(key => {
          const control = this.uploadForm.get(key);
          if (control?.invalid) {
            console.error(`❌ ${key} errors:`, control.errors);
          }
        });
      }
      this.toastService.error('Error', 'Please fill in all required fields and select a file');
      return;
    }

    // Validate expiry date if required
    if (this.isExpiryRequired()) {
      const expiryDate = this.uploadForm.get('expiryDate')?.value;
      if (!expiryDate) {
        this.toastService.error('Error', 'Expiry date is required for this document type');
        return;
      }

      const selectedDate = new Date(expiryDate);
      if (selectedDate < new Date()) {
        this.toastService.error('Error', 'Expiry date cannot be in the past');
        return;
      }
    } else {
      // Optional expiry date validation - only validate if provided
      const expiryDate = this.uploadForm.get('expiryDate')?.value;
      if (expiryDate) {
        const selectedDate = new Date(expiryDate);
        if (selectedDate < new Date()) {
          this.toastService.error('Error', 'Expiry date cannot be in the past');
          return;
        }
      }
    }

    this.uploadInProgress = true;
    const documentTypeId = parseInt(this.uploadForm.get('documentTypeId')?.value);
    const expiryDate = this.uploadForm.get('expiryDate')?.value || undefined;

    console.log('✅ Uploading with documentTypeId:', documentTypeId);
    console.log('✅ File:', this.selectedFile);
    console.log('✅ Expiry date:', expiryDate);

    this.documentService
      .uploadDocument(documentTypeId, this.selectedFile!, expiryDate)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: LegalDocument) => {
          this.uploadInProgress = false;
          console.log('✅ Document uploaded successfully:', response);
          this.toastService.success('Success', `Document uploaded successfully (ID: ${response.id})`);
          this.router.navigate(['/documents']);
        },
        error: (error) => {
          this.uploadInProgress = false;
          console.error('❌ Error uploading document:', error);
          console.error('❌ Error status:', error?.status);
          console.error('❌ Error message:', error?.message);
          this.toastService.error('Error', 'Failed to upload document. Please try again.');
        }
      });
  }

  cancel(): void {
    this.router.navigate(['/documents']);
  }

  getFileSizeDisplay(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
  }
}
