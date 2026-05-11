import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservationService } from '../../../core/services/reservation.service';
import { Reservation } from '../../../core/models/reservation.model';

@Component({
  selector: 'app-user-reservation-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reservation-list.html',
  styleUrl: './reservation-list.scss'
})
export class ReservationListComponent implements OnInit {

  reservations: Reservation[] = [];
  pagedReservations: Reservation[] = [];
  loading = true;

  // Pagination
  currentPage = 1;
  pageSize = 4;
  totalPages = 1;

  constructor(private reservationService: ReservationService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.reservationService.getAll().subscribe({
      next: (data) => {
        this.reservations = data;
        this.totalPages = Math.max(1, Math.ceil(data.length / this.pageSize));
        this.updatePage();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedReservations = this.reservations.slice(start, start + this.pageSize);
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
}
