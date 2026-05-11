import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Driver {
  id?: number;
  firstName: string;
  lastName: string;
  phone: string;
  licenseNumber: string;
  licenseType: 'B' | 'C' | 'D' | 'TC';
  licenseExpiryDate: string;
  experienceYears: number;
  status?: 'AVAILABLE' | 'ASSIGNED' | 'ON_LEAVE' | 'SUSPENDED';
  validationStatus?: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
  assignedVehicle?: any;
}

@Injectable({ providedIn: 'root' })
export class DriverService {
  private url = 'http://localhost:8081/api/drivers';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Driver[]> {
    return this.http.get<Driver[]>(this.url);
  }
  getById(id: number): Observable<Driver> {
    return this.http.get<Driver>(`${this.url}/${id}`);
  }
  getPending(): Observable<Driver[]> {
    return this.http.get<Driver[]>(`${this.url}/pending`);
  }
  create(d: Driver): Observable<Driver> {
    return this.http.post<Driver>(this.url, d);
  }
  update(id: number, d: Driver): Observable<Driver> {
    return this.http.put<Driver>(`${this.url}/${id}`, d);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
  approve(id: number): Observable<Driver> {
    return this.http.put<Driver>(`${this.url}/${id}/approve`, {});
  }
  reject(id: number, reason: string): Observable<Driver> {
    return this.http.put<Driver>(`${this.url}/${id}/reject`,
      { reason });
  }
  autoAssign(vehicleId: number): Observable<Driver> {
    return this.http.post<Driver>(
      `${this.url}/assign/${vehicleId}`, {});
  }
  unassign(id: number): Observable<Driver> {
    return this.http.put<Driver>(`${this.url}/${id}/unassign`, {});
  }
  uploadLicense(driverId: number, file: File): Observable<Driver> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<Driver>(
      `${this.url}/${driverId}/upload-license`, formData);
  }
}