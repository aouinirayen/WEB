import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class CookieService {

  /**
   * Set a cookie
   * @param name Cookie name
   * @param value Cookie value
   * @param days Expiration days (optional)
   */
  setCookie(name: string, value: string, days: number = 7): void {
    const expires = new Date();
    expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
    const expiresStr = `expires=${expires.toUTCString()}`;
    document.cookie = `${name}=${value};${expiresStr};path=/`;
  }

  /**
   * Get a cookie value
   * @param name Cookie name
   * @returns Cookie value or null if not found
   */
  getCookie(name: string): string | null {
    const nameEQ = name + '=';
    const cookies = document.cookie.split(';');
    for (let i = 0; i < cookies.length; i++) {
      let cookie = cookies[i].trim();
      if (cookie.indexOf(nameEQ) === 0) {
        return cookie.substring(nameEQ.length);
      }
    }
    return null;
  }

  /**
   * Delete a cookie
   * @param name Cookie name
   */
  deleteCookie(name: string): void {
    this.setCookie(name, '', -1);
  }
}
