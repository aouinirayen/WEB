import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface SparePart {
  id?: number;
  name: string;
  category: 'ENGINE' | 'BRAKES' | 'TIRES' | 'ELECTRICAL' | 'FILTERS' | 'TRANSMISSION' | 'BODYWORK' | 'HVAC' | 'OTHER';
  referenceCode: string;
  stockQuantity: number;
  minStockThreshold: number;
  unitCost: number;
  supplier?: string;
  notes?: string;
  lowStock?: boolean;
}

export interface MaintenancePartUsage {
  id?: number;
  maintenanceOrder: any;
  sparePart: SparePart;
  quantityUsed: number;
  unitCostAtUsage: number;
  totalCost: number;
  usedDate: string;
}

export interface PartUsageRequest {
  maintenanceOrderId: number;
  sparePartId: number;
  quantityUsed: number;
  usedDate?: string;
}

export interface PartSuggestion {
  partId: number;
  name: string;
  referenceCode: string;
  category: string;
  stockQuantity: number;
  unitCost: number;
  lowStock: boolean;
  reason: string;
}

@Injectable({ providedIn: 'root' })
export class SparePartService {
  private url     = `${environment.apiBaseUrl}/spare-parts`;
  private useUrl  = `${environment.apiBaseUrl}/part-usages`;

  constructor(private http: HttpClient) {}

  // Spare parts
  getAll(): Observable<SparePart[]>      { return this.http.get<SparePart[]>(this.url); }
  getById(id: number): Observable<SparePart> { return this.http.get<SparePart>(`${this.url}/${id}`); }
  getLowStock(): Observable<SparePart[]> { return this.http.get<SparePart[]>(`${this.url}/low-stock`); }
  create(p: SparePart): Observable<SparePart>       { return this.http.post<SparePart>(this.url, p); }
  update(id: number, p: SparePart): Observable<SparePart> { return this.http.put<SparePart>(`${this.url}/${id}`, p); }
  delete(id: number): Observable<void>  { return this.http.delete<void>(`${this.url}/${id}`); }

  // Auto-suggest
  suggest(maintenanceOrderId: number): Observable<PartSuggestion[]> {
    return this.http.get<PartSuggestion[]>(`${this.url}/suggest/${maintenanceOrderId}`);
  }

  // Part usages
  recordUsage(req: PartUsageRequest): Observable<MaintenancePartUsage> {
    return this.http.post<MaintenancePartUsage>(this.useUrl, req);
  }
  getUsageByOrder(orderId: number): Observable<MaintenancePartUsage[]> {
    return this.http.get<MaintenancePartUsage[]>(`${this.useUrl}/by-order/${orderId}`);
  }
  getCostForOrder(orderId: number): Observable<number> {
    return this.http.get<number>(`${this.useUrl}/cost/${orderId}`);
  }
  deleteUsage(id: number): Observable<void> {
    return this.http.delete<void>(`${this.useUrl}/${id}`);
  }
}
