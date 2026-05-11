// prediction-widget.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PredictionService, PartPrediction } from '../../services/prediction.service';

@Component({
  selector: 'app-prediction-widget',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './prediction-widget.component.html',
  styleUrls: ['./prediction-widget.component.css']
})
export class PredictionWidgetComponent implements OnInit {
  predictions: PartPrediction[] = [];
  loading = true;

  constructor(private service: PredictionService) {}

  ngOnInit() {
    this.service.getFleetPredictions().subscribe({
      next: data => {
        // Top 5 most urgent
        this.predictions = data
          .filter(p => p.urgency === 'URGENT' || p.urgency === 'SOON')
          .slice(0, 5);
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });
  }
}
