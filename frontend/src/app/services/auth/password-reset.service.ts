import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ForgetPasswordRequest {
  email: string;
}

export interface ForgetPasswordResponse {
  message: string;
}

export interface ResetPasswordRequest {
  resetToken: string;
  newPassword: string;
  confirmPassword: string;
}

export interface ResetPasswordResponse {
  message: string;
  username: string;
  email: string;
}

@Injectable({
  providedIn: 'root'
})
export class PasswordResetService {
  private apiUrl = '/api/auth';

  constructor(private http: HttpClient) {}

  forgetPassword(email: string): Observable<ForgetPasswordResponse> {
    const request: ForgetPasswordRequest = { email };
    console.log('Sending forget password request:', request);
    return this.http.post<ForgetPasswordResponse>(
      `${this.apiUrl}/forget-password`,
      request
    );
  }

  resetPassword(
    resetToken: string,
    newPassword: string,
    confirmPassword: string
  ): Observable<ResetPasswordResponse> {
    const request: ResetPasswordRequest = {
      resetToken,
      newPassword,
      confirmPassword
    };
    console.log('Sending reset password request with token:', resetToken);
    console.log('Full request payload:', JSON.stringify(request, null, 2));
    console.log('Sending to endpoint:', `${this.apiUrl}/reset-password`);
    return this.http.post<ResetPasswordResponse>(
      `${this.apiUrl}/reset-password`,
      request
    );
  }
}
