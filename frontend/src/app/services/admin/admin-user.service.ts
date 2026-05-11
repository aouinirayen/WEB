import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Page<T> {
  content: T[];
  pageable?: any;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort?: any;
  first?: boolean;
  last?: boolean;
  numberOfElements?: number;
}

export interface UserSearchCriteria {
  keyword?: string;
  role?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  email: string;
  name: string;
  role: string;
  CIN: number;
  photo?: string;
}

export interface UserUpdateRequest {
  username?: string;
  email?: string;
  name?: string;
  role?: string;
  CIN?: number;
  enabled?: boolean;
}

export interface UserResponse {
  id: number;
  username: string;
  email: string;
  name?: string;
  role: string;
  photoContentType?: string;
  createdAt?: string;
  updatedAt?: string;
  enabled?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminUserService {
  private apiUrl = '/api/admin/users';

  constructor(private http: HttpClient) {}

  private buildParams(criteria: UserSearchCriteria): HttpParams {
    let params = new HttpParams();
    if (criteria.keyword) {
      params = params.set('keyword', criteria.keyword);
    }
    if (criteria.role) {
      params = params.set('role', criteria.role);
    }
    if (criteria.page != null) {
      params = params.set('page', criteria.page.toString());
    }
    if (criteria.size != null) {
      params = params.set('size', criteria.size.toString());
    }
    if (criteria.sortBy) {
      params = params.set('sortBy', criteria.sortBy);
    }
    if (criteria.sortDir) {
      params = params.set('sortDir', criteria.sortDir);
    }
    return params;
  }

  getAllUsers(criteria: UserSearchCriteria = {}): Observable<Page<UserResponse>> {
    const params = this.buildParams(criteria);
    return this.http.get<Page<UserResponse>>(this.apiUrl, { params });
  }

  searchUsers(criteria: UserSearchCriteria = {}): Observable<Page<UserResponse>> {
    const params = this.buildParams(criteria);
    return this.http.get<Page<UserResponse>>(`${this.apiUrl}/search`, { params });
  }

  getUserById(id: number): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/${id}`);
  }

  createUser(request: UserCreateRequest): Observable<UserResponse> {
    return this.http.post<UserResponse>(this.apiUrl, request);
  }

  updateUser(id: number, request: UserUpdateRequest): Observable<UserResponse> {
    return this.http.put<UserResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  changeUserRole(id: number, role: string): Observable<UserResponse> {
    const params = new HttpParams().set('role', role);
    return this.http.patch<UserResponse>(`${this.apiUrl}/${id}/role`, null, { params });
  }

  uploadUserPhoto(id: number, file: File): Observable<UserResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UserResponse>(`${this.apiUrl}/${id}/photo`, formData);
  }

  getUserPhoto(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/photo`, {
      responseType: 'blob'
    });
  }

  deleteUserPhoto(id: number): Observable<UserResponse> {
    return this.http.delete<UserResponse>(`${this.apiUrl}/${id}/photo`);
  }

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }
}
