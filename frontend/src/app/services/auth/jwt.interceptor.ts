import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

@Injectable()
export class JwtInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // External AI tunnel endpoints should not receive app JWT.
    if (
      request.url.includes('ngrok-free.app') ||
      request.url.includes('ngrok.app') ||
      request.url.includes('trycloudflare.com')
    ) {
      return next.handle(request);
    }

    const token = this.authService.getToken();

    if (token) {
      // Add Authorization header with JWT token
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request);
  }
}
