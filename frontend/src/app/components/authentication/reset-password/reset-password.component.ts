import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { PasswordResetService } from '../../../services/auth/password-reset.service';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
  resetPasswordForm!: FormGroup;
  loading = false;
  submitted = false;
  successMessage = '';
  errorMessage = '';
  resetToken: string | null = null;
  showNewPassword = false;
  showConfirmPassword = false;
  private destroy$ = new Subject<void>();

  constructor(
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private passwordResetService: PasswordResetService
  ) {}

  ngOnInit(): void {
    // Initialize form first
    this.resetPasswordForm = this.formBuilder.group(
      {
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );

    // Extract token from URL query params
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        console.log('Query params received:', params);
        const token = params['token'];
        console.log('Token from URL:', token);

        if (token) {
          this.resetToken = token;
          this.errorMessage = ''; // Clear error if token exists
          console.log('Token set successfully:', this.resetToken);
        } else {
          this.errorMessage = 'Invalid or missing reset token. Please request a new password reset.';
          console.warn('No token found in URL query params');
        }
      });
  }

  // Custom validator to check if passwords match
  passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
    const newPassword = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!newPassword || !confirmPassword) {
      return null;
    }

    return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
  }

  get f() {
    return this.resetPasswordForm.controls;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  onSubmit(): void {
    this.submitted = true;
    this.successMessage = '';
    this.errorMessage = '';

    // Stop if form is invalid or no token
    if (this.resetPasswordForm.invalid || !this.resetToken) {
      if (!this.resetToken) {
        this.errorMessage = 'Invalid or missing reset token.';
        console.error('Cannot submit: resetToken is missing or null');
      }
      return;
    }

    this.loading = true;

    const newPassword = this.f['newPassword'].value;
    const confirmPassword = this.f['confirmPassword'].value;

    console.log('Submitting reset password with token:', this.resetToken);
    console.log('New password length:', newPassword.length);
    console.log('Passwords match:', newPassword === confirmPassword);

    this.passwordResetService
      .resetPassword(this.resetToken, newPassword, confirmPassword)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response: any) => {
          console.log('Reset password response:', response);
          this.successMessage = response.message || 'Password reset successfully! Redirecting to login...';
          this.loading = false;
          this.resetPasswordForm.reset();

          // Redirect to login after 2 seconds
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error: any) => {
          console.error('Reset password error full object:', error);
          console.error('Error status:', error.status);
          console.error('Error body:', error.error);
          console.error('Full error response:', JSON.stringify(error, null, 2));

          // Try to extract the most specific error message
          let errorMessage = 'Failed to reset password. Please try again.';

          if (error.error) {
            if (typeof error.error === 'string') {
              errorMessage = error.error;
            } else if (error.error.error) {
              errorMessage = error.error.error;
            } else if (error.error.message) {
              errorMessage = error.error.message;
            } else if (error.error.errors && Array.isArray(error.error.errors) && error.error.errors.length > 0) {
              errorMessage = error.error.errors[0];
            } else if (error.error.detail) {
              errorMessage = error.error.detail;
            }
          } else if (error.message) {
            errorMessage = error.message;
          }

          console.error('Final error message:', errorMessage);
          this.errorMessage = errorMessage;
          this.loading = false;
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
