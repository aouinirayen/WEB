import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { Ticket, TransportType } from '../models/ticket.model';
import { throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class TicketService {

  private url = '/api/tickets';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Ticket[]> {
    return this.http.get<Ticket[]>(this.url);
  }

  getDisponibles(): Observable<Ticket[]> {
    const url = `${this.url}/disponibles`;
    console.log('[TicketService] 🔍 GET request to:', url);
    return this.http.get<Ticket[]>(url).pipe(
      tap(data => {
        console.log('[TicketService] ✓ Response received:', {
          url: url,
          count: data.length,
          data: data,
          timestamp: new Date().toISOString()
        });
      }),
      catchError(error => {
        console.error('[TicketService] ✗ Request failed:', {
          url: url,
          status: error.status,
          statusText: error.statusText,
          error: error.error,
          timestamp: new Date().toISOString()
        });
        return throwError(() => error);
      })
    );
  }

  getByTransport(type: string): Observable<Ticket[]> {
    const url = `${this.url}/transport/${type}`;
    console.log('[TicketService] 🔍 GET request to:', url);
    return this.http.get<Ticket[]>(url).pipe(
      tap(data => {
        console.log('[TicketService] ✓ Response received:', {
          url: url,
          transportType: type,
          count: data.length,
          data: data
        });
      }),
      catchError(error => {
        console.error('[TicketService] ✗ Request failed:', {
          url: url,
          status: error.status,
          error: error.error
        });
        return throwError(() => error);
      })
    );
  }

  getDisponiblesByTransport(type: string): Observable<Ticket[]> {
    const url = `${this.url}/disponibles/${type}`;
    console.log('[TicketService] 🔍 GET request to:', url);
    return this.http.get<Ticket[]>(url).pipe(
      tap(data => {
        console.log('[TicketService] ✓ Response received:', {
          url: url,
          transportType: type,
          count: data.length,
          data: data
        });
      }),
      catchError(error => {
        console.error('[TicketService] ✗ Request failed:', {
          url: url,
          status: error.status,
          error: error.error
        });
        return throwError(() => error);
      })
    );
  }

  getTransportTypes(): Observable<TransportType[]> {
    const url = `${this.url}/transport-types`;
    console.log('[TicketService] 🔍 GET request to:', url);
    return this.http.get<TransportType[]>(url).pipe(
      tap(data => {
        console.log('[TicketService] ✓ Transport types received:', {
          url: url,
          count: data.length,
          data: data
        });
      }),
      catchError(error => {
        console.error('[TicketService] ✗ Request failed:', {
          url: url,
          status: error.status,
          error: error.error
        });
        return throwError(() => error);
      })
    );
  }

  getById(id: number): Observable<Ticket> {
    return this.http.get<Ticket>(`${this.url}/${id}`);
  }

  create(ticket: Ticket): Observable<Ticket> {
    return this.http.post<Ticket>(this.url, ticket);
  }

  update(id: number, ticket: Ticket): Observable<Ticket> {
    return this.http.put<Ticket>(`${this.url}/${id}`, ticket);
  }

  delete(id: number): Observable<any> {
    return this.http.delete(`${this.url}/${id}`);
  }

  acheter(id: number, passengerId?: number): Observable<Ticket> {
    const params = passengerId ? `?passengerId=${passengerId}` : "";
    return this.http.post<Ticket>(`${this.url}/${id}/acheter${params}`, {});
  }

  // DEBUG METHOD - Test backend connectivity
  debugBackend(): void {
    console.log('='.repeat(60));
    console.log('[DEBUG] Testing Backend Connectivity');
    console.log('='.repeat(60));
    console.log('API Base URL:', this.url);
    console.log('Full endpoint would be:', `http://localhost:4200${this.url}/disponibles`);
    console.log('Time:', new Date().toISOString());

    // Test 1: Direct endpoint call with response logging
    console.log('\n[DEBUG TEST 1] Calling /api/tickets/disponibles...');
    this.http.get(`${this.url}/disponibles`, { responseType: 'json' }).pipe(
      tap((response: any) => {
        console.log('[DEBUG] ✓ Test 1 Success - Raw Response:', {
          responseType: typeof response,
          isArray: Array.isArray(response),
          length: Array.isArray(response) ? response.length : 'N/A',
          content: response
        });
      }),
      catchError(error => {
        console.error('[DEBUG] ✗ Test 1 Failed:', {
          status: error.status,
          statusText: error.statusText,
          message: error.message,
          error: error.error,
          headers: error.headers
        });
        return throwError(() => error);
      })
    ).subscribe();

    // Test 2: Check if backend is responding at all
    console.log('\n[DEBUG TEST 2] Checking backend health at /api/tickets...');
    this.http.get(`${this.url}`, { responseType: 'json' }).pipe(
      tap(() => {
        console.log('[DEBUG] ✓ Test 2 Success - Backend is responding');
      }),
      catchError(error => {
        console.error('[DEBUG] ✗ Test 2 Failed - Backend not responding:', {
          status: error.status,
          error: error.error
        });
        return throwError(() => error);
      })
    ).subscribe();

    console.log('='.repeat(60));
  }
}

