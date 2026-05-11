import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Vehicle } from '../models/models';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private url = 'http://localhost:8081/api/vehicles';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Vehicle[]> {
    return this.http.get<Vehicle[]>(this.url);
  }
  getById(id: number): Observable<Vehicle> {
    return this.http.get<Vehicle>(`${this.url}/${id}`);
  }
  create(v: Vehicle): Observable<Vehicle> {
    return this.http.post<Vehicle>(this.url, v);
  }
  update(id: number, v: Vehicle): Observable<Vehicle> {
    return this.http.put<Vehicle>(`${this.url}/${id}`, v);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}