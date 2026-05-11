import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService, RegisterRequest } from '../../../services/auth/auth.service';

export function cinValidator(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const value = control.value.toString();
  if (!/^\d{8}$/.test(value)) return { 'invalidCin': true };
  return null;
}

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit, OnDestroy {
  registerForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  success = '';
  showPassword = false;
  passwordStrength = 0;
  passwordStrengthText = '';
  passwordStrengthColor = '';

  roles = ['AGENT', 'OPERATOR', 'PASSENGER'];
  roleOptions: { label: string; value: string }[] = [];

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
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.roleOptions = this.roles.map(role => ({ label: role, value: role }));

    this.registerForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      email: ['', [Validators.required, Validators.email]],
      name: ['', [Validators.required]],
      CIN: ['', [Validators.required, cinValidator]],
      role: ['AGENT', [Validators.required]]
    });

    this.registerForm.get('password')?.valueChanges.subscribe((password: string) => {
      this.calculatePasswordStrength(password);
    });

    this.slideInterval = setInterval(() => {
      this.currentSlide = (this.currentSlide + 1) % this.transportImages.length;
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.slideInterval) clearInterval(this.slideInterval);
  }

  get f() {
    return this.registerForm.controls;
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  calculatePasswordStrength(password: string): void {
    if (!password) { this.passwordStrength = 0; this.passwordStrengthText = ''; return; }
    let score = 0;
    if (password.length >= 8) score += 25;
    if (password.length >= 12) score += 15;
    if (/[A-Z]/.test(password)) score += 20;
    if (/[0-9]/.test(password)) score += 20;
    if (/[^A-Za-z0-9]/.test(password)) score += 20;
    this.passwordStrength = Math.min(score, 100);
    if (score < 40) { this.passwordStrengthText = 'Weak'; this.passwordStrengthColor = '#dc2626'; }
    else if (score < 70) { this.passwordStrengthText = 'Fair'; this.passwordStrengthColor = '#f59e0b'; }
    else if (score < 90) { this.passwordStrengthText = 'Good'; this.passwordStrengthColor = '#3b82f6'; }
    else { this.passwordStrengthText = 'Strong'; this.passwordStrengthColor = '#16a34a'; }
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';
    this.success = '';

    if (this.registerForm.invalid) return;

    this.loading = true;

    const registerRequest: RegisterRequest = {
      username: this.f['username'].value,
      password: this.f['password'].value,
      email: this.f['email'].value,
      name: this.f['name'].value,
      CIN: Number(this.f['CIN'].value),
      role: this.f['role'].value
    };

    this.authService.register(registerRequest).subscribe({
      next: () => {
        this.success = 'Account created! Redirecting to login...';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
