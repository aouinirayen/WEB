import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CovoiturageService } from '../../core/services/covoiturage.service';
import { ReservationService } from '../../core/services/reservation.service';
import { Covoiturage } from '../../core/models/covoiturage.model';
import { Reservation } from '../../core/models/reservation.model';
import { MapPickerComponent } from '../../shared/map-picker/map-picker';

@Component({
  selector: 'app-my-covoiturages',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MapPickerComponent],
  templateUrl: './my-covoiturages.html',
  styleUrl: './my-covoiturages.scss'
})
export class MyCovoituragesComponent implements OnInit {

  // View mode: 'driver' or 'passenger'
  viewMode: 'driver' | 'passenger' = 'driver';
  userName = '';
  searched = false;

  // Driver view data
  myCovoiturages: Covoiturage[] = [];
  pagedCovoiturages: Covoiturage[] = [];
  allReservations: Reservation[] = [];
  visibleClientMapId: number | null = null;
  expandedReservationId: number | null = null;

  // Pagination
  currentPage = 1;
  pageSize = 3;
  totalPages = 1;

  // Passenger view data
  myReservations: Reservation[] = [];
  allCovoiturages: Covoiturage[] = [];

  constructor(
    private covoiturageService: CovoiturageService,
    private reservationService: ReservationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {}

  search(): void {
    if (!this.userName.trim()) return;
    this.searched = true;

    this.covoiturageService.getAll().subscribe({
      next: covoiturages => {
        this.myCovoiturages = covoiturages.filter(
          c => c.driverName.toLowerCase() === this.userName.trim().toLowerCase()
        );
        this.totalPages = Math.max(1, Math.ceil(this.myCovoiturages.length / this.pageSize));
        this.currentPage = 1;
        this.updatePage();
        this.reservationService.getAll().subscribe({
          next: reservations => {
            this.allReservations = reservations;
            this.cdr.detectChanges();
          }
        });
      }
    });
  }

  getReservationsForCovoiturage(covoiturageId: number): Reservation[] {
    return this.allReservations.filter(r => r.covoiturageId === covoiturageId);
  }

  confirmReservation(r: Reservation): void {
    r.status = 'CONFIRMED';
    this.reservationService.update(r.id!, r).subscribe({
      next: () => this.search(),
      error: err => console.error('Error confirming', err)
    });
  }

  toggleClientMap(reservationId: number): void {
    this.visibleClientMapId = this.visibleClientMapId === reservationId ? null : reservationId;
  }

  toggleReservationDetails(reservationId: number): void {
    this.expandedReservationId = this.expandedReservationId === reservationId ? null : reservationId;
  }

  rejectReservation(r: Reservation): void {
    r.status = 'REJECTED';
    this.reservationService.update(r.id!, r).subscribe({
      next: () => this.search(),
      error: err => console.error('Error rejecting', err)
    });
  }

  getCovoiturageForReservation(covoiturageId: number): Covoiturage | undefined {
    return this.allCovoiturages.find(c => c.id === covoiturageId);
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedCovoiturages = this.myCovoiturages.slice(start, start + this.pageSize);
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
