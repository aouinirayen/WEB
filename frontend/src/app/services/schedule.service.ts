import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Schedule } from '../models/models';

@Injectable({ providedIn: 'root' })
export class ScheduleService {
  private url = 'http://localhost:8081/api/schedules';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(this.url);
  }
  getByLine(lineId: number): Observable<Schedule[]> {
    return this.http.get<Schedule[]>(`${this.url}/line/${lineId}`);
  }
  create(s: Schedule): Observable<Schedule> {
    return this.http.post<Schedule>(this.url, s);
  }
  update(id: number, s: Schedule): Observable<Schedule> {
    return this.http.put<Schedule>(`${this.url}/${id}`, s);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
    
  }
  getById(id: number): Observable<Schedule> {
    return this.http.get<Schedule>(`${this.url}/${id}`);
  }
}