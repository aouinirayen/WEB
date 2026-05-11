import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { CookieService } from '../shared/cookie.service';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  role: string;
  CIN: number;
  photo?: string;
}

export interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  name: string;
  role: string;
}

export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  phone?: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = '/api/auth';
  private tokenCookieName = 'jwt_token';
  private userCookieName = 'current_user';

  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  private isAuthenticatedSubject: BehaviorSubject<boolean>;
  public isAuthenticated$: Observable<boolean>;

  constructor(
    private http: HttpClient,
    private cookieService: CookieService
  ) {
    this.currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
    this.currentUser$ = this.currentUserSubject.asObservable();

    this.isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
    this.isAuthenticated$ = this.isAuthenticatedSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get isAuthenticated(): boolean {
    return this.isAuthenticatedSubject.value;
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, request)
      .pipe(
        map(response => {
          // Store JWT token in cookie
          this.cookieService.setCookie(this.tokenCookieName, response.token, 7);

          // Store user info in cookie
          const user: User = {
            id: response.id,
            username: response.username,
            email: response.email,
            name: response.name,
            role: response.role
          };
          this.cookieService.setCookie(this.userCookieName, JSON.stringify(user), 7);

          // Update subjects
          this.currentUserSubject.next(user);
          this.isAuthenticatedSubject.next(true);

          return response;
        })
      );
  }

  register(request: RegisterRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, request);
  }

  logout(): void {
    // Remove token and user from cookies
    this.cookieService.deleteCookie(this.tokenCookieName);
    this.cookieService.deleteCookie(this.userCookieName);

    // Update subjects
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  getToken(): string | null {
    return this.cookieService.getCookie(this.tokenCookieName);
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  private getUserFromStorage(): User | null {
    const userStr = this.cookieService.getCookie(this.userCookieName);
    return userStr ? JSON.parse(userStr) : null;
  }

  isLoggedIn(): boolean {
    return this.hasToken() && this.currentUserSubject.value != null;
  }
}
