import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Trip } from '../models/models';

@Injectable({ providedIn: 'root' })
export class TripService {
  private url = 'http://localhost:8081/api/trips';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Trip[]> {
    return this.http.get<Trip[]>(this.url);
  }
  getBySchedule(scheduleId: number): Observable<Trip[]> {
    return this.http.get<Trip[]>(`${this.url}/schedule/${scheduleId}`);
  }
  create(t: Trip): Observable<Trip> {
    return this.http.post<Trip>(this.url, t);
  }
  update(id: number, t: Trip): Observable<Trip> {
    return this.http.put<Trip>(`${this.url}/${id}`, t);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
  getById(id: number): Observable<Trip> {
    return this.http.get<Trip>(`${this.url}/${id}`);
  }
}