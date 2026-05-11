import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface MatchRequest {
  passengerLat: number;
  passengerLng: number;
  destLat: number;
  destLng: number;
  heureVoulue: string;
  budgetMax: number;
}

export interface MatchResult {
  covoiturageId: number;
  driverName: string;
  departure: string;
  destination: string;
  heureDepart: string;
  heureArrivee: string;
  date: string;
  price: number;
  availableSeats: number;
  vehicle: string;
  score: number;
  recommendation: string;
  distanceToDeparture: number;
  distanceToDestination: number;
}

export interface CancellationRequest {
  prix: number;
  distanceKm: number;
  joursAvant: number;
  heure: number;
  nbPlaces: number;
}

export interface CancellationResponse {
  probability: number;
  riskLevel: string;
  message: string;
}

export interface SatisfactionRequest {
  covoiturageId: number | null;
  matchScore: number;
  prixRatio: number;
  ponctualite: number;
  placesRatio: number;
  detourKm: number;
}

export interface SatisfactionResponse {
  predictedScore: number;
  stars: number;
  message: string;
  driverName: string;
  route: string;
  driverAvgScore: number;
  driverTotalRatings: number;
}

export interface CovoiturageOption {
  id: number;
  label: string;
}

export interface DriverRatingInfo {
  driverName: string;
  avgScore: number;
  totalRatings: number;
}

export interface AIStats {
  matchingModel: any;
  cancellationModel: any;
  satisfactionModel: any;
  totalTrainingData: number;
  status: string;
}

export interface AIHealth {
  status: string;
  matching: boolean;
  cancellation: boolean;
  satisfaction: boolean;
}

@Injectable({ providedIn: 'root' })
export class AIService {

  private url = '/api/ai';

  constructor(private http: HttpClient) {}

  findMatches(req: MatchRequest): Observable<MatchResult[]> {
    return this.http.post<MatchResult[]>(`${this.url}/matching`, req);
  }

  predictCancellation(req: CancellationRequest): Observable<CancellationResponse> {
    return this.http.post<CancellationResponse>(`${this.url}/cancellation`, req);
  }

  predictSatisfaction(req: SatisfactionRequest): Observable<SatisfactionResponse> {
    return this.http.post<SatisfactionResponse>(`${this.url}/satisfaction`, req);
  }

  getStats(): Observable<AIStats> {
    return this.http.get<AIStats>(`${this.url}/stats`);
  }

  getHealth(): Observable<AIHealth> {
    return this.http.get<AIHealth>(`${this.url}/health`);
  }

  retrain(): Observable<any> {
    return this.http.post(`${this.url}/retrain`, {});
  }

  getCovoituragesList(): Observable<CovoiturageOption[]> {
    return this.http.get<CovoiturageOption[]>(`${this.url}/covoiturages-list`);
  }

  getDriverRatings(): Observable<DriverRatingInfo[]> {
    return this.http.get<DriverRatingInfo[]>(`${this.url}/driver-ratings`);
  }
}
