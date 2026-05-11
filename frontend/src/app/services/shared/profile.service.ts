import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { ProfileResponse, ProfileUpdateRequest } from '../../models/profile.model';

interface CurrentUserResponse {
  id: number;
  role: string;
  enabled?: boolean;
}

interface AdminUserUpdateRequest {
  username: string;
  email: string;
  name: string;
  role: string;
  enabled: boolean;
  cin?: number;
}

/**
 * Service for user profile operations
 * Handles all profile-related API calls to the backend
 */
@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private apiUrl = '/api/profile';
  private adminUsersApiUrl = '/api/admin/users';
  private profileSubject = new BehaviorSubject<ProfileResponse | null>(null);
  public profile$ = this.profileSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Get current user's profile
   */
  getCurrentProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(this.apiUrl).pipe(
      tap(profile => this.profileSubject.next(profile))
    );
  }

  /**
   * Update current user's profile
   */
  updateProfile(request: ProfileUpdateRequest): Observable<ProfileResponse> {
    const requestedUsername = request.username?.trim();
    const profileRequest: ProfileUpdateRequest = {
      email: request.email,
      name: request.name,
      ...(request.cin != null ? { cin: request.cin } : {})
    };

    return this.http.put<ProfileResponse>(this.apiUrl, profileRequest).pipe(
      switchMap((updatedProfile) => {
        if (!requestedUsername || requestedUsername === updatedProfile.username) {
          return of(updatedProfile);
        }

        return this.http.get<CurrentUserResponse>(`${this.adminUsersApiUrl}/me`).pipe(
          switchMap((currentUser) => {
            const adminUpdateRequest: AdminUserUpdateRequest = {
              username: requestedUsername,
              email: profileRequest.email,
              name: profileRequest.name,
              role: currentUser.role,
              enabled: currentUser.enabled ?? true,
              ...(profileRequest.cin != null ? { cin: profileRequest.cin } : {})
            };

            return this.http.put(`${this.adminUsersApiUrl}/${currentUser.id}`, adminUpdateRequest).pipe(
              switchMap(() => this.http.get<ProfileResponse>(this.apiUrl)),
              catchError(() => of(updatedProfile))
            );
          }),
          catchError(() => of(updatedProfile))
        );
      }),
      tap(profile => this.profileSubject.next(profile))
    );
  }

  /**
   * Upload profile photo
   */
  uploadPhoto(file: File): Observable<ProfileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<ProfileResponse>(`${this.apiUrl}/photo`, formData).pipe(
      tap(profile => this.profileSubject.next(profile))
    );
  }

  /**
   * Get profile photo
   */
  getPhoto(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/photo`, { responseType: 'blob' });
  }

  /**
   * Delete profile photo
   */
  deletePhoto(): Observable<ProfileResponse> {
    return this.http.delete<ProfileResponse>(`${this.apiUrl}/photo`).pipe(
      tap(profile => this.profileSubject.next(profile))
    );
  }

  /**
   * Get current cached profile
   */
  getCachedProfile(): ProfileResponse | null {
    return this.profileSubject.value;
  }

  /**
   * Clear cached profile
   */
  clearProfile(): void {
    this.profileSubject.next(null);
  }
}
