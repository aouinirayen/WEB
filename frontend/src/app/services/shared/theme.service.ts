import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ThemeMode = 'light' | 'dark';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly storageKey = 'app_theme_preference';
  private readonly themeSubject = new BehaviorSubject<ThemeMode>(this.resolveInitialTheme());
  theme$ = this.themeSubject.asObservable();

  constructor() {
    this.applyTheme(this.themeSubject.value);
  }

  private resolveInitialTheme(): ThemeMode {
    const saved = localStorage.getItem(this.storageKey) as ThemeMode | null;
    if (saved === 'dark' || saved === 'light') {
      return saved;
    }
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    return prefersDark ? 'dark' : 'light';
  }

  private applyTheme(theme: ThemeMode): void {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem(this.storageKey, theme);
    this.themeSubject.next(theme);
  }

  toggleTheme(): void {
    const nextTheme: ThemeMode = this.themeSubject.value === 'dark' ? 'light' : 'dark';
    this.applyTheme(nextTheme);
  }
}
