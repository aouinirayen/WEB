import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService, LoginRequest } from '../../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm!: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  returnUrl: string = '';
  showPassword = false;
  activeTab = 'login';

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
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loginForm = this.formBuilder.group({
      username: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });

    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '';

    // Start slideshow
    this.slideInterval = setInterval(() => {
      this.currentSlide = (this.currentSlide + 1) % this.transportImages.length;
    }, 5000);
  }

  ngOnDestroy(): void {
    if (this.slideInterval) {
      clearInterval(this.slideInterval);
    }
  }

  get f() {
    return this.loginForm.controls;
  }

  goToSlide(index: number): void {
    this.currentSlide = index;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;

    const loginRequest: LoginRequest = {
      username: this.f['username'].value,
      password: this.f['password'].value
    };

    this.authService.login(loginRequest).subscribe({
      next: (response: any) => {
        console.log('Login successful:', response);
        const role = (response.role || '').toString().toUpperCase();
        let targetRoute = '/home';

        if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
          targetRoute = '/admin/dashboard';
        } else if (role === 'AGENT' || role === 'ROLE_AGENT') {
          targetRoute = '/agent-dhasbord';
        } else if (role === 'OPERATOR' || role === 'ROLE_OPERATOR') {
          targetRoute = '/operator-dhasbord';
        } else if (role === 'PASSENGER' || role === 'ROLE_PASSENGER') {
          targetRoute = '/passenger-dhasbord';
        }

        if (this.returnUrl) {
          this.router.navigateByUrl(this.returnUrl);
        } else {
          this.router.navigate([targetRoute]);
        }
      },
      error: (err: any) => {
        this.error = err?.error?.message || 'Invalid username or password. Please try again.';
        this.loading = false;
      }
    });
  }
}
