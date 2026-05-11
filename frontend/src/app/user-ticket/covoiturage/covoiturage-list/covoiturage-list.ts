import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { ReservationService } from '../../../core/services/reservation.service';
import { Covoiturage } from '../../../core/models/covoiturage.model';
import { Reservation } from '../../../core/models/reservation.model';
import { MapPickerComponent } from '../../../shared/map-picker/map-picker';

@Component({
  selector: 'app-user-covoiturage-list',
  standalone: true,
  imports: [CommonModule, RouterLink, MapPickerComponent],
  templateUrl: './covoiturage-list.html',
  styleUrl: './covoiturage-list.scss'
})
export class CovoiturageListComponent implements OnInit {

  covoiturages: Covoiturage[] = [];
  pagedCovoiturages: Covoiturage[] = [];
  allReservations: Reservation[] = [];
  loading = true;
  expandedId: number | null = null;
  visibleDepartureMapId: number | null = null;

  // Pagination
  currentPage = 1;
  pageSize = 6;
  totalPages = 1;

  constructor(
    private covoiturageService: CovoiturageService,
    private reservationService: ReservationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.covoiturageService.getAll().subscribe({
      next: (data) => {
        this.covoiturages = data.filter(c => !c.status || c.status === 'CONFIRMED');
        this.totalPages = Math.max(1, Math.ceil(this.covoiturages.length / this.pageSize));
        this.updatePage();
        this.reservationService.getAll().subscribe({
          next: (res) => {
            this.allReservations = res.filter(r => r.status === 'CONFIRMED');
            this.loading = false;
            this.cdr.detectChanges();
          },
          error: () => { this.loading = false; this.cdr.detectChanges(); }
        });
      },
      error: () => { this.loading = false; this.cdr.detectChanges(); }
    });
  }

  toggleDetails(id: number): void {
    this.expandedId = this.expandedId === id ? null : id;
  }

  toggleDepartureMap(id: number): void {
    this.visibleDepartureMapId = this.visibleDepartureMapId === id ? null : id;
  }

  getRemainingSeats(c: Covoiturage): number {
    const reserved = this.allReservations
      .filter(r => r.covoiturageId === c.id)
      .reduce((sum, r) => sum + r.seatsReserved, 0);
    return Math.max(0, c.availableSeats - reserved);
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedCovoiturages = this.covoiturages.slice(start, start + this.pageSize);
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
