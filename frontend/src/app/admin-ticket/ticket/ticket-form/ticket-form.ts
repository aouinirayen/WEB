import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { TicketService } from '../../../core/services/ticket.service';
import { Ticket, TransportType } from '../../../core/models/ticket.model';

@Component({
  selector: 'app-admin-ticket-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ticket-form.html',
  styleUrl: './ticket-form.scss'
})
export class TicketFormComponent implements OnInit {

  ticket: Ticket = {
    type: '', price: 0, description: '', validity: '',
    transportType: '', quantiteDisponible: 1, lieuDepart: '', destination: '', heureDepart: ''
  };
  isEdit = false;
  transportTypes: TransportType[] = [];
  backendErrors: string[] = [];
  saving = false;

  constructor(
    private ticketService: TicketService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.ticketService.getTransportTypes().subscribe({
      next: data => this.transportTypes = data,
      error: () => {
        this.transportTypes = [
          { value: 'BUS', label: 'Bus' },
          { value: 'METRO', label: 'Metro' },
          { value: 'TRAIN', label: 'Train' },
          { value: 'LOUAGE', label: 'Shared Taxi' },
          { value: 'BATEAU', label: 'Boat' }
        ];
      }
    });

    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true;
      this.ticketService.getById(+id).subscribe(data => this.ticket = data);
    }
  }

  save(): void {
    this.backendErrors = [];
    this.saving = true;
    const op = this.isEdit
      ? this.ticketService.update(this.ticket.id!, this.ticket)
      : this.ticketService.create(this.ticket);

    op.subscribe({
      next: () => this.router.navigate(['/admin/ticket/tickets']),
      error: (err) => {
        this.saving = false;
        if (err.error?.errors) {
          this.backendErrors = err.error.errors;
        } else if (err.error?.message) {
          this.backendErrors = [err.error.message];
        } else {
          this.backendErrors = ['An error occurred.'];
        }
      }
    });
  }
}
