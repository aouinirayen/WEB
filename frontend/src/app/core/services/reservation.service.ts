import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Reservation } from '../models/reservation.model';

@Injectable({ providedIn: 'root' })
export class ReservationService {

  private url = '/api/reservations';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.url);
  }

  getById(id: number): Observable<Reservation> {
    return this.http.get<Reservation>(`${this.url}/${id}`);
  }

  create(r: Reservation): Observable<Reservation> {
    return this.http.post<Reservation>(this.url, r);
  }

  update(id: number, r: Reservation): Observable<Reservation> {
    return this.http.put<Reservation>(`${this.url}/${id}`, r);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.url}/${id}`);
  }

  canRate(covoiturageId: number, clientName: string): Observable<{ canRate: boolean }> {
    return this.http.get<{ canRate: boolean }>(`${this.url}/can-rate`, {
      params: { covoiturageId: covoiturageId.toString(), clientName }
    });
  }

  getRatableCovoiturages(clientName: string): Observable<{ id: number; label: string }[]> {
    return this.http.get<{ id: number; label: string }[]>(`${this.url}/ratable-covoiturages`, {
      params: { clientName }
    });
  }
}

