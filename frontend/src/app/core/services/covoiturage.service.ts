import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Covoiturage, DriverConfiance, AvisList } from '../models/covoiturage.model';

@Injectable({ providedIn: 'root' })
export class CovoiturageService {

  private url = 'http://localhost:8081/api/covoiturages';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Covoiturage[]> {
    return this.http.get<Covoiturage[]>(this.url);
  }

  getById(id: number): Observable<Covoiturage> {
    return this.http.get<Covoiturage>(`${this.url}/${id}`);
  }

  create(c: Covoiturage): Observable<Covoiturage> {
    return this.http.post<Covoiturage>(this.url, c);
  }

  update(id: number, c: Covoiturage): Observable<Covoiturage> {
    return this.http.put<Covoiturage>(`${this.url}/${id}`, c);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.url}/${id}`);
  }

  // === CONFIANCE & AVIS METHODS ===

  getConfiance(driverName: string): Observable<DriverConfiance> {
    return this.http.get<DriverConfiance>(`${this.url}/confiance/${driverName}`);
  }

  addAvis(covoiturageId: number, stars: number): Observable<DriverConfiance> {
    return this.http.post<DriverConfiance>(`${this.url}/${covoiturageId}/avis`, null, {
      params: { stars: stars.toString() }
    });
  }

  confirmer(covoiturageId: number): Observable<DriverConfiance> {
    return this.http.put<DriverConfiance>(`${this.url}/${covoiturageId}/confirmer`, null);
  }

  seedTestConfiance(): Observable<string> {
    return this.http.post<string>(`${this.url}/seed-test-confiance`, null);
  }

  getAvisByDriver(driverName: string): Observable<AvisList> {
    return this.http.get<AvisList>(`${this.url}/avis/${driverName}`);
  }
}

