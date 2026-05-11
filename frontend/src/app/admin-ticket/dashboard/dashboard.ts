import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { TicketService } from '../../core/services/ticket.service';
import { CovoiturageService } from '../../core/services/covoiturage.service';
import { ReservationService } from '../../core/services/reservation.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss'
})
export class DashboardComponent implements OnInit {

  ticketCount = 0;
  covoiturageCount = 0;
  reservationCount = 0;

  constructor(
    private ticketService: TicketService,
    private covoiturageService: CovoiturageService,
    private reservationService: ReservationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.ticketService.getAll().subscribe({
      next: data => { this.ticketCount = data.length; this.cdr.detectChanges(); },
      error: () => this.ticketCount = 0
    });
    this.covoiturageService.getAll().subscribe({
      next: data => { this.covoiturageCount = data.length; this.cdr.detectChanges(); },
      error: () => this.covoiturageCount = 0
    });
    this.reservationService.getAll().subscribe({
      next: data => { this.reservationCount = data.length; this.cdr.detectChanges(); },
      error: () => this.reservationCount = 0
    });
  }
}
