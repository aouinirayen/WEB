import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FuelLog } from '../models/models';

@Injectable({ providedIn: 'root' })
export class FuelLogService {
  private url = 'http://localhost:8081/api/fuel-logs';

  constructor(private http: HttpClient) {}

  getAll(): Observable<FuelLog[]> {
    return this.http.get<FuelLog[]>(this.url);
  }
  getById(id: number): Observable<FuelLog> {
    return this.http.get<FuelLog>(`${this.url}/${id}`);
  }
  getByVehicle(vehicleId: number): Observable<FuelLog[]> {
    return this.http.get<FuelLog[]>(`${this.url}/vehicle/${vehicleId}`);
  }
  create(f: FuelLog): Observable<FuelLog> {
    return this.http.post<FuelLog>(this.url, f);
  }
  update(id: number, f: FuelLog): Observable<FuelLog> {
    return this.http.put<FuelLog>(`${this.url}/${id}`, f);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}
