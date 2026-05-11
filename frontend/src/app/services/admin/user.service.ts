import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort?: any;
  first?: boolean;
  last?: boolean;
}

export interface UserFilterParams {
  page?: number;
  size?: number;
  keyword?: string;
  role?: string;
  status?: string;
  sortBy?: string;
  sortDir?: string;
}

export interface UserDto {
  id: number;
  username: string;
  email: string;
  name: string;
  role: string;
  cin?: number;
  enabled: boolean;
  inactivatedUntil?: string; // ISO datetime when user becomes active again, null if permanent or no ban
  createdAt?: string;
  updatedAt?: string;
  photoUrl?: string; // Base64 or blob URL for display
}

export interface UserCreatePayload {
  username: string;
  password: string;
  email: string;
  name: string;
  role: string;
  enabled: boolean;
  CIN?: number;
}

export interface UserUpdatePayload {
  username?: string;
  email?: string;
  name?: string;
  role?: string;
  cin?: number;
  enabled?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly apiUrl = '/api/admin/users';

  constructor(private http: HttpClient) {}

  private buildParams(params: UserFilterParams = {}): HttpParams {
    let httpParams = new HttpParams();
    if (params.page != null) {
      httpParams = httpParams.set('page', params.page.toString());
    }
    if (params.size != null) {
      httpParams = httpParams.set('size', params.size.toString());
    }
    if (params.keyword) {
      httpParams = httpParams.set('keyword', params.keyword);
    }
    if (params.role && params.role !== 'ALL') {
      httpParams = httpParams.set('role', params.role);
    }
    if (params.status && params.status !== 'all') {
      httpParams = httpParams.set('status', params.status);
    }
    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.sortDir) {
      httpParams = httpParams.set('sortDir', params.sortDir);
    }
    return httpParams;
  }

  getUsers(params: UserFilterParams = {}): Observable<PaginatedResponse<UserDto>> {
    const httpParams = this.buildParams(params);
    return this.http.get<PaginatedResponse<UserDto>>(this.apiUrl, { params: httpParams });
  }

  searchUsers(params: UserFilterParams = {}): Observable<PaginatedResponse<UserDto>> {
    const httpParams = this.buildParams(params);
    return this.http.get<PaginatedResponse<UserDto>>(`${this.apiUrl}/search`, { params: httpParams });
  }

  getUserById(id: number): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}/${id}`);
  }

  createUser(payload: UserCreatePayload): Observable<UserDto> {
    return this.http.post<UserDto>(this.apiUrl, payload);
  }

  updateUser(id: number, payload: UserUpdatePayload): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${id}`, payload);
  }

  updateUserRole(id: number, role: string): Observable<UserDto> {
    return this.http.patch<UserDto>(`${this.apiUrl}/${id}/role`, { role });
  }

  updateUserStatus(id: number, enabled: boolean): Observable<UserDto> {
    return this.http.patch<UserDto>(`${this.apiUrl}/${id}/status`, { enabled });
  }

  banUser(id: number, durationDays: number | null): Observable<UserDto> {
    // durationDays: 1, 3, 7, 30 for temporary bans, or null for permanent ban
    return this.http.patch<UserDto>(`${this.apiUrl}/${id}/ban`, { durationDays });
  }

  unbanUser(id: number): Observable<UserDto> {
    return this.http.patch<UserDto>(`${this.apiUrl}/${id}/unban`, {});
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  deleteUsers(ids: number[]): Observable<void> {
    // Delete users one by one to avoid CORS preflight issues with bulk DELETE
    // This approach doesn't trigger CORS preflight for simple individual DELETE requests
    if (ids.length === 0) {
      return of(void 0);
    }

    // Delete first user, then recursively delete the rest
    return this.deleteUser(ids[0]).pipe(
      switchMap(() => {
        if (ids.length > 1) {
          return this.deleteUsers(ids.slice(1));
        }
        return of(void 0);
      })
    );
  }

  uploadUserPhoto(id: number, file: File): Observable<UserDto> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UserDto>(`${this.apiUrl}/${id}/photo`, formData);
  }

  getUserPhoto(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/photo`, {
      responseType: 'blob'
    });
  }

  /** Load user photo and convert to data URL for display */
  loadUserPhotoUrl(user: UserDto): Promise<string | undefined> {
    if (!user.id) {
      return Promise.resolve(undefined);
    }

    return this.http.get(`${this.apiUrl}/${user.id}/photo`, {
      responseType: 'blob'
    }).pipe(
      catchError((error) => {
        // Silently handle 404 and other errors - photo may not exist
        if (error.status === 404) {
          return of(new Blob());
        }
        return of(new Blob());
      })
    ).toPromise().then(blob => {
      if (blob && blob.size > 0) {
        return this.blobToDataUrl(blob);
      }
      return undefined;
    }).catch(() => undefined);
  }

  /** Convert blob to base64 data URL */
  private blobToDataUrl(blob: Blob): string {
    return URL.createObjectURL(blob);
  }

  exportUsers(params: UserFilterParams = {}): Observable<Blob> {
    const httpParams = this.buildParams(params);
    return this.http.get(`${this.apiUrl}/export`, {
      params: httpParams,
      responseType: 'blob'
    });
  }
}
