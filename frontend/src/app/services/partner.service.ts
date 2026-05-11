import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Partner } from '../models/partner';

@Injectable({
  providedIn: 'root'
})
export class PartnerService {

  private apiUrl = '/api/partners';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Partner[]> {
    return this.http.get<Partner[]>(this.apiUrl);
  }

  getById(id: number): Observable<Partner> {
    return this.http.get<Partner>(`${this.apiUrl}/${id}`);
  }

  create(partner: Partner): Observable<Partner> {
    return this.http.post<Partner>(this.apiUrl, partner);
  }

  update(id: number, partner: Partner): Observable<Partner> {
    return this.http.put<Partner>(`${this.apiUrl}/${id}`, partner);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getByOrganizationId(orgId: number): Observable<Partner[]> {
    return this.http.get<Partner[]>(`${this.apiUrl}/by-organization/${orgId}`);
  }
}

