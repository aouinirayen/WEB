import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { User, RoleEnum } from '../models/models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(private router: Router) {}

  private getCookie(name: string): string | null {
    const escaped = name.replace(/[-[\]/{}()*+?.\\^$|]/g, '\\$&');
    const match = document.cookie.match(new RegExp(`(?:^|; )${escaped}=([^;]*)`));
    return match ? decodeURIComponent(match[1]) : null;
  }

  saveToken(token: string) { localStorage.setItem('token', token); }
  getToken(): string | null {
    return localStorage.getItem('token') ?? this.getCookie('jwt_token');
  }
  saveUser(user: User) { localStorage.setItem('user', JSON.stringify(user)); }
  clearSession() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  private readJsonFromStorage<T = any>(key: string): T | null {
    const raw = localStorage.getItem(key);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  }

  private normalizeStoredUser(raw: any): User | null {
    if (!raw || typeof raw !== 'object') return null;
    const idCandidate = raw.id ?? raw.userId ?? raw.user_id;
    const id = Number(idCandidate);
    if (!Number.isFinite(id) || id <= 0) return null;
    return {
      ...raw,
      id
    } as User;
  }

  getUser(): User | null {
    const fromUser = this.normalizeStoredUser(this.readJsonFromStorage('user'));
    if (fromUser) return fromUser;

    const fromCurrentUser = this.normalizeStoredUser(this.readJsonFromStorage('currentUser'));
    if (fromCurrentUser) {
      this.saveUser(fromCurrentUser);
      return fromCurrentUser;
    }

    const fromUserProfile = this.normalizeStoredUser(this.readJsonFromStorage('userProfile'));
    if (fromUserProfile) {
      this.saveUser(fromUserProfile);
      return fromUserProfile;
    }

    const fromCookie = this.normalizeStoredUser(
      (() => {
        const raw = this.getCookie('current_user');
        if (!raw) return null;
        try {
          return JSON.parse(raw);
        } catch {
          return null;
        }
      })()
    );
    if (fromCookie) {
      this.saveUser(fromCookie);
      return fromCookie;
    }

    return null;
  }

  getUserId(): number | null {
    const id = this.getUser()?.id;
    return Number.isFinite(id) && (id as number) > 0 ? (id as number) : null;
  }

  getRole(): RoleEnum | null {
    return this.getUser()?.role ?? null;
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isOperator(): boolean { return this.getRole() === RoleEnum.OPERATOR; }
  isPassenger(): boolean { return this.getRole() === RoleEnum.PASSENGER; }
  isAdmin(): boolean { return this.getRole() === RoleEnum.ADMIN; }

  logout() {
    this.clearSession();
    this.router.navigate(['/login']);
  }

  getInitials(): string {
    const u = this.getUser();
    if (!u) return 'U';
    return u.name ? u.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2) : u.username.slice(0, 2).toUpperCase();
  }
}
