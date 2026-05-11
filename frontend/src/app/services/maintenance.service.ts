import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MaintenanceOrder } from '../models/models';

@Injectable({ providedIn: 'root' })
export class MaintenanceService {
  private url = 'http://localhost:8081/api/maintenance';
  constructor(private http: HttpClient) {}

  getAll(): Observable<MaintenanceOrder[]> {
    return this.http.get<MaintenanceOrder[]>(this.url);
  }
  getById(id: number): Observable<MaintenanceOrder> {
    return this.http.get<MaintenanceOrder>(`${this.url}/${id}`);
  }
  getByVehicle(vehicleId: number): Observable<MaintenanceOrder[]> {
    return this.http.get<MaintenanceOrder[]>(
      `${this.url}/vehicle/${vehicleId}`);
  }
  getAlerts(): Observable<MaintenanceOrder[]> {
    return this.http.get<MaintenanceOrder[]>(`${this.url}/alerts`);
  }
  create(m: MaintenanceOrder): Observable<MaintenanceOrder> {
    return this.http.post<MaintenanceOrder>(this.url, m);
  }
  update(id: number, m: MaintenanceOrder): Observable<MaintenanceOrder> {
    return this.http.put<MaintenanceOrder>(`${this.url}/${id}`, m);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}