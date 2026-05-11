import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface StopNode {
  id: number;
  name: string;
  sequence: number;
  latitude: number;
  longitude: number;
  distanceFromPrevKm: number;
  weightScore: number;
  roadType: 'HIGHWAY' | 'URBAN' | 'RURAL';
}

export interface RouteResult {
  lineId: number;
  lineCode: string;
  method: 'DIJKSTRA' | 'WEIGHTED_AI';
  optimalRoute: StopNode[];
  originalRoute: StopNode[];
  totalDistanceKm: number;
  estimatedFuelLiters: number;
  estimatedDurationMin: number;
  efficiencyScore: number;
  explanation: string;
  trafficLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  trafficDelayMin: number;
}

export interface VehiclePosition {
  id: number;
  vehicle: { id: number; plateNumber: string; brand: string; type: string };
  latitude: number;
  longitude: number;
  speedKmh: number;
  heading: string;
  updatedAt: string;
  simulated: boolean;
  currentStopIndex: number;
  status: string;
}

@Injectable({ providedIn: 'root' })
export class RoutingService {
  private base = environment.apiBaseUrl;
  constructor(private http: HttpClient) {}

  optimizeRoute(lineId: number): Observable<RouteResult> {
    return this.http.get<RouteResult>(`${this.base}/routes/optimize/${lineId}`);
  }

  getAllPositions(): Observable<VehiclePosition[]> {
    return this.http.get<VehiclePosition[]>(`${this.base}/gps/positions`);
  }

  getVehiclePosition(vehicleId: number): Observable<VehiclePosition> {
    return this.http.get<VehiclePosition>(`${this.base}/gps/positions/${vehicleId}`);
  }

  updateRealGps(vehicleId: number, lat: number, lon: number): Observable<VehiclePosition> {
    return this.http.post<VehiclePosition>(
      `${this.base}/gps/positions/${vehicleId}`, { lat, lon });
  }
}
