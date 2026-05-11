import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { ReservationService } from '../../../core/services/reservation.service';
import { Reservation } from '../../../core/models/reservation.model';

@Component({
  selector: 'app-admin-reservation-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './reservation-form.html',
  styleUrl: './reservation-form.scss'
})
export class ReservationFormComponent implements OnInit {

  reservation: Reservation = {
    clientName: '', phone: '', seatsReserved: 1, bookingDate: '', status: 'PENDING'
  };
  isEdit = false;

  constructor(
    private reservationService: ReservationService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true;
      this.reservationService.getById(+id).subscribe(data => this.reservation = data);
    }
  }

  save(): void {
    if (this.isEdit) {
      this.reservationService.update(this.reservation.id!, this.reservation).subscribe(() => {
        this.router.navigate(['/admin/ticket/reservations']);
      });
    } else {
      this.reservationService.create(this.reservation).subscribe(() => {
        this.router.navigate(['/admin/ticket/reservations']);
      });
    }
  }
}
