import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute, RouterLink } from '@angular/router';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { Covoiturage } from '../../../core/models/covoiturage.model';
import { MapPickerComponent } from '../../../shared/map-picker/map-picker';

@Component({
  selector: 'app-admin-covoiturage-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, MapPickerComponent],
  templateUrl: './covoiturage-form.html',
  styleUrl: './covoiturage-form.scss'
})
export class CovoiturageFormComponent implements OnInit {

  covoiturage: Covoiturage = {
    driverName: '', departure: '', destination: '', date: '',
    heureDepart: '', heureArrivee: '',
    price: 0, availableSeats: 0, vehicle: ''
  };
  today = new Date().toISOString().split('T')[0];
  minTime = this.getMinTime();
  isEdit = false;

  constructor(
    private covoiturageService: CovoiturageService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.params['id'];
    if (id) {
      this.isEdit = true;
      this.covoiturageService.getById(+id).subscribe(data => this.covoiturage = data);
    }
  }

  getMinTime(): string {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 30);
    return now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');
  }

  get isHeureDepartInvalid(): boolean {
    if (!this.covoiturage.heureDepart || this.covoiturage.date !== this.today) return false;
    return this.covoiturage.heureDepart < this.minTime;
  }

  onDepartureLocationSelected(event: { lat: number; lng: number }): void {
    this.covoiturage.departureLat = event.lat;
    this.covoiturage.departureLng = event.lng;
  }

  save(): void {
    if (this.isEdit) {
      this.covoiturageService.update(this.covoiturage.id!, this.covoiturage).subscribe(() => {
        this.router.navigate(['/admin/ticket/covoiturages']);
      });
    } else {
      this.covoiturageService.create(this.covoiturage).subscribe(() => {
        this.router.navigate(['/admin/ticket/covoiturages']);
      });
    }
  }
}
