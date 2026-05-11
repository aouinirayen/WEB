import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Line } from '../models/models';

@Injectable({ providedIn: 'root' })
export class LineService {
  private url = 'http://localhost:8081/api/lines';
  constructor(private http: HttpClient) {}

  getAll(): Observable<Line[]> {
    return this.http.get<Line[]>(this.url);
  }
  getById(id: number): Observable<Line> {
    return this.http.get<Line>(`${this.url}/${id}`);
  }
  create(l: Line): Observable<Line> {
    return this.http.post<Line>(this.url, l);
  }
  update(id: number, l: Line): Observable<Line> {
    return this.http.put<Line>(`${this.url}/${id}`, l);
  }
  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}