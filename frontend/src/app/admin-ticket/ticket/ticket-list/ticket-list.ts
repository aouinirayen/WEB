import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-admin-ticket-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.scss'
})
export class TicketListComponent implements OnInit {

  tickets: Ticket[] = [];
  pagedTickets: Ticket[] = [];

  // Pagination
  currentPage = 1;
  pageSize = 4;
  totalPages = 1;

  constructor(private ticketService: TicketService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.loadTickets();
  }

  loadTickets(): void {
    this.ticketService.getAll().subscribe({
      next: data => {
        this.tickets = data;
        this.totalPages = Math.max(1, Math.ceil(data.length / this.pageSize));
        if (this.currentPage > this.totalPages) this.currentPage = 1;
        this.updatePage();
        this.cdr.detectChanges();
      },
      error: err => console.error('Error loading tickets', err)
    });
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

  deleteTicket(id: number): void {
    if (confirm('Delete this ticket?')) {
      this.ticketService.delete(id).subscribe(() => this.loadTickets());
    }
  }
}
