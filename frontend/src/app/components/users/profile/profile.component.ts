import { Component, OnInit, OnDestroy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ProfileService } from '../../../services/shared/profile.service';
import { ToastService } from '../../../services/shared/toast.service';
import { ProfileResponse, ProfileUpdateRequest } from '../../../models/profile.model';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

/**
 * Custom validator for CIN - must be exactly 8 digits
 */
export function cinValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) {
    return null; // Don't validate empty values
  }
  const value = control.value.toString();
  if (!/^\d{8}$/.test(value)) {
    return { 'invalidCin': true };
  }
  return null;
}

/**
 * User Profile Component
 * Allows users to view and edit their profile information
 * Supports profile photo upload and deletion
 */
@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit, OnDestroy {
  profileForm!: FormGroup;
  profile: ProfileResponse | null = null;
  profilePhotoUrl: string | null = null;
  loading = false;
  editing = false;
  photoLoading = false;
  photoSelected = false;
  selectedFile: File | null = null;
  private destroy$ = new Subject<void>();

  // Error tracking for individual fields
  fieldErrors: { [key: string]: string } = {};

  // Direct URL base for files served from htdocs
  private fileServerUrl = 'http://localhost:8081/pidev-uploads/';

  constructor(
    private profileService: ProfileService,
    private formBuilder: FormBuilder,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.initializeForm();
    this.loadProfileData();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Initialize the profile form
   */
  private initializeForm(): void {
    this.profileForm = this.formBuilder.group({
      email: [{ value: '', disabled: true }, [Validators.required, Validators.email]],
      name: [{ value: '', disabled: true }, [Validators.required, Validators.minLength(2)]],
      cin: [{ value: '', disabled: true }, [cinValidator]]
    });
  }

  /**
   * Load current user's profile
   */
  private loadProfileData(): void {
    this.loading = true;
    this.profileService.getCurrentProfile()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profile) => {
          this.profile = profile;
          this.populateForm(profile);
          this.loadProfilePhoto();
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading profile:', error);
          this.toastService.error('Error', 'Failed to load profile information');
          this.loading = false;
        }
      });
  }

  /**
   * Populate form with profile data
   */
  private populateForm(profile: ProfileResponse): void {
    this.profileForm.patchValue({
      username: profile.username,
      email: profile.email,
      name: profile.name,
      cin: profile.cin || ''
    });
  }

  /**
   * Load profile photo using direct URL from htdocs
   */
  private loadProfilePhoto(): void {
    if (this.profile?.photoPath) {
      // Normalize path: convert backslashes to forward slashes (in case backend returns Windows path)
      let normalizedPath = this.profile.photoPath.replace(/\\/g, '/');

      // Remove absolute path if backend returns full path
      if (normalizedPath.includes('pidev-uploads/')) {
        normalizedPath = normalizedPath.split('pidev-uploads/')[1];
      }

      this.profilePhotoUrl = this.fileServerUrl + normalizedPath;
      console.log('Profile photo URL:', this.profilePhotoUrl);
      console.log('Backend photoPath:', this.profile.photoPath);
    } else {
      this.profilePhotoUrl = null;
      console.log('No photo path found in profile');
    }
  }

  /**
   * Format date string for display
   */
  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleString();
  }

  /**
   * Enable edit mode
   */
  startEditing(): void {
    this.editing = true;
    this.profileForm.get('username')?.enable();
    this.profileForm.get('email')?.enable();
    this.profileForm.get('name')?.enable();
    this.profileForm.get('cin')?.enable();
  }

  /**
   * Cancel editing and revert changes
   */
  cancelEditing(): void {
    this.editing = false;
    if (this.profile) {
      this.populateForm(this.profile);
      this.profileForm.get('username')?.disable();
      this.profileForm.get('email')?.disable();
      this.profileForm.get('name')?.disable();
      this.profileForm.get('cin')?.disable();
    }
    this.fieldErrors = {};
  }

  /**
   * Save profile changes
   */
  saveProfile(): void {
    if (this.profileForm.invalid) {
      this.markFormGroupTouched(this.profileForm);
      return;
    }

    this.loading = true;
    const raw = this.profileForm.getRawValue();
    const requestedUsername = (raw.username ?? '').trim();
    const cinValue = typeof raw.cin === 'string' ? raw.cin.trim() : raw.cin;
    const parsedCin = cinValue === '' || cinValue == null ? undefined : Number(cinValue);

    const updateRequest: ProfileUpdateRequest = {
      username: requestedUsername,
      email: (raw.email ?? '').trim(),
      name: (raw.name ?? '').trim(),
      ...(Number.isFinite(parsedCin) ? { cin: parsedCin } : {})
    };

    this.profileService.updateProfile(updateRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.populateForm(updatedProfile);
          this.editing = false;
          this.profileForm.get('username')?.disable();
          this.profileForm.get('email')?.disable();
          this.profileForm.get('name')?.disable();
          this.profileForm.get('cin')?.disable();
          this.loading = false;
          this.fieldErrors = {};

          if (requestedUsername && updatedProfile.username !== requestedUsername) {
            this.fieldErrors['username'] = 'Username update is not allowed by the current backend endpoint/permissions.';
            this.toastService.warning('Partial Update', 'Profile updated, but username was not changed.');
          } else {
            this.toastService.success('Successs', 'Profile updated successfully');
          }
        },
        error: (error: unknown) => {
          console.error('Error updating profile:', error);
          this.loading = false;
          this.fieldErrors = {};

          const backendMessage = this.getBackendErrorMessage(error);

          // Handle specific error messages
          if (backendMessage.includes('Email already exists')) {
            this.fieldErrors['email'] = 'Email already exists. Please use a different email.';
            this.toastService.error('Error', 'Email already exists');
          } else {
            this.toastService.error('Error', backendMessage || 'Failed to update profile');
          }
        }
      });
  }

  private getBackendErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const err = error.error as any;
      if (typeof err === 'string' && err.trim()) {
        return err;
      }
      if (err && typeof err.message === 'string' && err.message.trim()) {
        return err.message;
      }
      if (err && typeof err.error === 'string' && err.error.trim()) {
        return err.error;
      }
      return error.statusText || `Request failed (${error.status})`;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return 'Failed to update profile';
  }

  /**
   * Handle file selection for photo upload
   */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files;

    if (files && files.length > 0) {
      const file = files[0];

      // Validate file type
      const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
      if (!allowedTypes.includes(file.type)) {
        this.toastService.error('Error', 'Only JPEG, PNG, GIF, and WebP images are allowed');
        this.photoSelected = false;
        return;
      }

      // Validate file size (5MB max)
      const maxSize = 5 * 1024 * 1024; // 5MB
      if (file.size > maxSize) {
        this.toastService.error('Error', 'File size exceeds maximum limit of 5MB');
        this.photoSelected = false;
        return;
      }

      this.selectedFile = file;
      this.photoSelected = true;

      // Preview the image
      const reader = new FileReader();
      reader.onload = (e: ProgressEvent<FileReader>) => {
        this.profilePhotoUrl = e.target?.result as string;
      };
      reader.readAsDataURL(file);
    }
  }

  /**
   * Upload profile photo
   */
  uploadPhoto(): void {
    if (!this.selectedFile) {
      this.toastService.error('Error', 'Please select a photo first');
      return;
    }

    this.photoLoading = true;
    this.profileService.uploadPhoto(this.selectedFile)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.photoLoading = false;
          this.photoSelected = false;
          this.selectedFile = null;
          this.toastService.success('Successs', 'Profile photo uploaded successfully');
        },
        error: (error) => {
          console.error('Error uploading photo:', error);
          this.photoLoading = false;
          this.toastService.error('Error', error.error?.message || 'Failed to upload profile photo');
        }
      });
  }

  /**
   * Delete profile photo
   */
  deletePhoto(): void {
    if (!confirm('Are you sure you want to delete your profile photo?')) {
      return;
    }

    this.photoLoading = true;
    this.profileService.deletePhoto()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedProfile) => {
          this.profile = updatedProfile;
          this.profilePhotoUrl = null;
          this.photoLoading = false;
          this.toastService.success('Successs', 'Profile photo deleted successfully');
        },
        error: (error) => {
          console.error('Error deleting photo:', error);
          this.photoLoading = false;
          this.toastService.error('Error', 'Failed to delete profile photo');
        }
      });
  }

  /**
   * Cancel photo upload
   */
  cancelPhotoUpload(): void {
    this.photoSelected = false;
    this.selectedFile = null;
    if (!this.profile?.photoContentType || !this.profilePhotoUrl) {
      this.profilePhotoUrl = null;
    }
    this.loadProfilePhoto();
  }

  /**
   * Get form control errors
   */
  getFieldError(fieldName: string): string {
    const errors = this.fieldErrors[fieldName];
    if (!errors) return '';

    // Handle CIN validation
    if (fieldName === 'cin') {
      if (errors === 'CIN must be exactly 8 digits') {
        return errors;
      }
    }
    return errors;
  }

  /**
   * Check if field has error
   */
  hasFieldError(fieldName: string): boolean {
    return this.fieldErrors[fieldName] !== undefined;
  }

  /**
   * Mark all form fields as touched
   */
  private markFormGroupTouched(formGroup: FormGroup): void {
    Object.keys(formGroup.controls).forEach(key => {
      const control = formGroup.get(key);
      control?.markAsTouched();

      if (control?.invalid && control?.errors) {
        if (control.errors['required']) {
          this.fieldErrors[key] = `${key.charAt(0).toUpperCase() + key.slice(1)} is required`;
        } else if (control.errors['email']) {
          this.fieldErrors[key] = 'Please enter a valid email address';
        } else if (control.errors['minlength']) {
          this.fieldErrors[key] = `${key.charAt(0).toUpperCase() + key.slice(1)} must be at least ${control.errors['minlength'].requiredLength} characters`;
        } else if (control.errors['invalidCin']) {
          this.fieldErrors[key] = 'CIN must be exactly 8 digits';
        }
      }
    });
  }

  /**
   * Check if form is valid for editing
   */
  get isFormValid(): boolean {
    return this.profileForm.valid && this.editing;
  }

  /**
   * Check if has profile photo
   */
  get hasProfilePhoto(): boolean {
    return !!this.profilePhotoUrl;
  }

  /**
   * Get edit button text
   */
  get editButtonText(): string {
    return this.editing ? 'Cancel' : 'Edit Profile';
  }

  /**
   * Get profile display status
   */
  get displayStatus(): string {
    if (this.loading) return 'Loading...';
    if (this.editing) return 'Editing Profile';
    return 'View Profile';
  }
}
