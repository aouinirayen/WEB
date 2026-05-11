import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PredictionService, PartPrediction } from '../../services/prediction.service';
import { VehicleService } from '../../services/vehicle.service';
import { Vehicle } from '../../models/models';

@Component({
  selector: 'app-prediction-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './prediction-page.component.html',
  styleUrls: ['./prediction-page.component.css']
})
export class PredictionPageComponent implements OnInit {
  predictions: PartPrediction[] = [];
  vehicles: Vehicle[] = [];
  loading = false;

  scope: string | number = 'fleet';

  filterUrgency = '';
  filterMethod  = '';
  filterPart    = '';

  constructor(
    private predService: PredictionService,
    private vehicleService: VehicleService
  ) {}

  ngOnInit() {
    this.vehicleService.getAll().subscribe(v => this.vehicles = v);
    this.load();
  }

  onScopeChange() { this.load(); }

  load() {
    this.loading = true;
    const obs = this.scope === 'fleet'
      ? this.predService.getFleetPredictions()
      : this.predService.getVehiclePredictions(+this.scope);

    obs.subscribe({
      next: data => { this.predictions = data; this.loading = false; },
      error: ()   => { this.loading = false; }
    });
  }

  filtered(): PartPrediction[] {
    return this.predictions.filter(p => {
      const urgencyOk = !this.filterUrgency || p.urgency === this.filterUrgency;
      const methodOk  = !this.filterMethod  || p.method  === this.filterMethod;
      const nameOk    = !this.filterPart    ||
        p.partName.toLowerCase().includes(this.filterPart.toLowerCase());
      return urgencyOk && methodOk && nameOk;
    });
  }

  countByUrgency(u: string): number {
    return this.predictions.filter(p => p.urgency === u).length;
  }

  countByMethod(m: string): number {
    return this.predictions.filter(p => p.method === m).length;
  }

  confClass(score: number): string {
    if (score >= 0.80) return 'conf-high';
    if (score >= 0.60) return 'conf-med';
    return 'conf-low';
  }
}
