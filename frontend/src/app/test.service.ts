import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class TestService {

  constructor(private http: HttpClient) { }

  getHello() {
    return this.http.get(`${environment.apiBaseUrl}/hello`, { responseType: 'text' });
  }
}
