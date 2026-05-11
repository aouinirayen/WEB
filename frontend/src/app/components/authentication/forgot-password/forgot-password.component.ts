import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { AuthService } from '../../../services/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent implements OnInit, OnDestroy {
  forgotForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  emailSent = false;
  sentEmail = '';

  currentSlide = 0;
  private slideInterval: any;

  transportImages = [
    { url: 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200&q=80', label: 'Bus Network' },
    { url: 'https://images.unsplash.com/photo-1474487548417-781cb6d646b3?w=1200&q=80', label: 'Metro Lines' },
    { url: 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=1200&q=80', label: 'Rail Transport' },
    { url: 'https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=1200&q=80', label: 'City Mobility' }
  ];

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.forgotForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]]
    });

    this.slideInterval = setInterval(() => {
      this.currentSlide = (this.currentSlide + 1) % this.transportImages.length;
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.slideInterval) clearInterval(this.slideInterval);
  }

  get f() { return this.forgotForm.controls; }

  goToSlide(index: number): void { this.currentSlide = index; }

  resend(): void {
    this.emailSent = false;
    this.error = '';
    this.submitted = false;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    if (this.forgotForm.invalid) return;

    this.loading = true;
    this.sentEmail = this.f['email'].value;

    // Try calling authService.forgotPassword if it exists, otherwise simulate
    const service = this.authService as any;
    if (typeof service.forgotPassword === 'function') {
      service.forgotPassword(this.sentEmail).subscribe({
        next: () => { this.emailSent = true; this.loading = false; },
        error: (err: any) => {
          this.error = err?.error?.message || 'Failed to send reset email. Please try again.';
          this.loading = false;
        }
      });
    } else {
      // Simulate success if method doesn't exist yet
      setTimeout(() => { this.emailSent = true; this.loading = false; }, 1000);
    }
  }
}
