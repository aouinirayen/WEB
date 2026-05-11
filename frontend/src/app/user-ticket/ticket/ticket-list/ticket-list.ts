import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../../core/services/ticket.service';
import { AuthService } from '../../../core/services/auth.service';
import { Ticket, TransportType } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-user-ticket-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.scss',
})
export class TicketListComponent implements OnInit {

  tickets: Ticket[] = [];
  pagedTickets: Ticket[] = [];
  loading = true;
  transportTypes: TransportType[] = [];
  selectedTransport = '';
  acheterMessage = '';
  acheterError = '';

  // Pagination
  currentPage = 1;
  pageSize = 6;
  totalPages = 1;

  constructor(private ticketService: TicketService, private cdr: ChangeDetectorRef, private authService: AuthService) {}

  ngOnInit(): void {
    console.log('[TicketListComponent] Component initialized');
    this.ticketService.getTransportTypes().subscribe({
      next: data => {
        console.log('[TicketListComponent] Transport types loaded:', data);
        this.transportTypes = data;
      },
      error: (err) => {
        console.error('[TicketListComponent] Error loading transport types:', err);
        this.transportTypes = [
          { value: 'BUS', label: 'Bus' },
          { value: 'METRO', label: 'Metro' },
          { value: 'TRAIN', label: 'Train' },
          { value: 'LOUAGE', label: 'Shared Taxi' },
          { value: 'BATEAU', label: 'Boat' }
        ];
      }
    });
    this.loadTickets();
  }

  loadTickets(): void {
    console.log('[TicketListComponent] loadTickets() called, selectedTransport:', this.selectedTransport);
    this.loading = true;
    const obs = this.selectedTransport
      ? this.ticketService.getDisponiblesByTransport(this.selectedTransport)
      : this.ticketService.getDisponibles();

    obs.subscribe({
      next: (data) => {
        console.log('[TicketListComponent] ✓ Tickets received:', {
          count: data.length,
          data: data,
          timestamp: new Date().toISOString()
        });
        this.tickets = data;
        this.totalPages = Math.max(1, Math.ceil(data.length / this.pageSize));
        this.currentPage = 1;
        this.updatePage();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error('[TicketListComponent] ✗ Error loading tickets:', {
          status: err.status,
          statusText: err.statusText,
          url: err.url,
          message: err.message,
          error: err.error,
          fullError: err,
          timestamp: new Date().toISOString()
        });
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  filterByTransport(type: string): void {
    this.selectedTransport = this.selectedTransport === type ? '' : type;
    this.loadTickets();
  }

  debugBackend(): void {
    console.log('[TicketListComponent] Debug button clicked - calling debugBackend()');
    this.ticketService.debugBackend();
  }

  acheter(ticket: Ticket): void {
    if (!ticket.id || (ticket.quantiteDisponible ?? 0) <= 0) return;
    this.acheterMessage = '';
    this.acheterError = '';
    this.ticketService.acheter(ticket.id, this.authService.getUserId() ?? undefined).subscribe({
      next: (updated) => {
        ticket.quantiteDisponible = updated.quantiteDisponible;
        this.acheterMessage = `Ticket "${ticket.lieuDepart} → ${ticket.destination}" purchased successfully!`;
        this.cdr.detectChanges();
        setTimeout(() => { this.acheterMessage = ''; this.cdr.detectChanges(); }, 4000);
      },
      error: (err) => {
        this.acheterError = err.error?.message || err.error?.errors?.[0] || 'Error during purchase.';
        this.cdr.detectChanges();
        setTimeout(() => { this.acheterError = ''; this.cdr.detectChanges(); }, 4000);
      }
    });
  }

  getTransportIcon(type: string | undefined): string {
    switch (type) {
      case 'BUS': return 'fas fa-bus';
      case 'METRO': return 'fas fa-subway';
      case 'TRAIN': return 'fas fa-train';
      case 'LOUAGE': return 'fas fa-shuttle-van';
      case 'BATEAU': return 'fas fa-ship';
      default: return 'fas fa-ticket-alt';
    }
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedTickets = this.tickets.slice(start, start + this.pageSize);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) return;
    this.currentPage = page;
    this.updatePage();
  }

  getPages(): number[] {
    const pages: number[] = [];
    const maxVisible = 5;
    let start = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
    let end = start + maxVisible - 1;
    if (end > this.totalPages) { end = this.totalPages; start = Math.max(1, end - maxVisible + 1); }
    for (let i = start; i <= end; i++) pages.push(i);
    return pages;
  }

  getTransportColor(type: string | undefined): string {
    switch (type) {
      case 'BUS': return '#1565c0';
      case 'METRO': return '#6a1b9a';
      case 'TRAIN': return '#2e7d32';
      case 'LOUAGE': return '#e65100';
      case 'BATEAU': return '#00838f';
      default: return '#3f51b5';
    }
  }
}
