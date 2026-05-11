import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReservationService } from '../../../core/services/reservation.service';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { Reservation } from '../../../core/models/reservation.model';
import { Covoiturage } from '../../../core/models/covoiturage.model';

@Component({
  selector: 'app-admin-reservation-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './reservation-list.html',
  styleUrl: './reservation-list.scss'
})
export class ReservationListComponent implements OnInit {

  reservations: Reservation[] = [];
  pagedReservations: Reservation[] = [];
  covoiturages: Covoiturage[] = [];

  // Pagination
  currentPage = 1;
  pageSize = 4;
  totalPages = 1;

  constructor(
    private reservationService: ReservationService,
    private covoiturageService: CovoiturageService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.covoiturageService.getAll().subscribe({
      next: data => {
        this.covoiturages = data;
        this.load();
      }
    });
  }

  load(): void {
    this.reservationService.getAll().subscribe({
      next: data => {
        this.reservations = data;
        this.totalPages = Math.max(1, Math.ceil(data.length / this.pageSize));
        if (this.currentPage > this.totalPages) this.currentPage = 1;
        this.updatePage();
        this.cdr.detectChanges();
      },
      error: err => console.error('Error loading reservations', err)
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

  getCovoiturageForReservation(covoiturageId?: number): Covoiturage | undefined {
    if (!covoiturageId) return undefined;
    return this.covoiturages.find(c => c.id === covoiturageId);
  }
}
