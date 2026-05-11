import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface PartPrediction {
  partName: string;
  referenceCode: string;
  category: string;
  currentStock: number;
  predictedQuantityNeeded: number;
  predictedDateNeeded: string;
  daysUntilNeeded: number;
  confidenceScore: number;
  method: 'FREQUENCY_ANALYSIS' | 'LINEAR_REGRESSION';
  urgency: 'URGENT' | 'SOON' | 'MONITOR';
  explanation: string;
  affectedVehicles: string[];
  vehicleId?: number;
  vehiclePlate?: string;
}

@Injectable({ providedIn: 'root' })
export class PredictionService {
  private url = `${environment.apiBaseUrl}/predictions`;
  constructor(private http: HttpClient) {}

  getFleetPredictions(): Observable<PartPrediction[]> {
    return this.http.get<PartPrediction[]>(`${this.url}/fleet`);
  }

  getVehiclePredictions(vehicleId: number): Observable<PartPrediction[]> {
    return this.http.get<PartPrediction[]>(`${this.url}/vehicle/${vehicleId}`);
  }
}
