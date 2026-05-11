import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Stop } from '../models/models';

@Injectable({ providedIn: 'root' })
export class StopService {
  private url = 'http://localhost:8081/api/stops';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Stop[]> {
    return this.http.get<Stop[]>(this.url);
  }
  getByLine(lineId: number): Observable<Stop[]> {
    return this.http.get<Stop[]>(`${this.url}/line/${lineId}`);
  }
  create(s: Stop): Observable<Stop> {
    return this.http.post<Stop>(this.url, s);
  }
  update(id: number, s: Stop): Observable<Stop> {
    return this.http.put<Stop>(`${this.url}/${id}`, s);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
    
  }
  getById(id: number): Observable<Stop> {
    return this.http.get<Stop>(`${this.url}/${id}`);
  }
}