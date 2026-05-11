import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AIService, AIStats, AIHealth } from '../../core/services/ai.service';

@Component({
  selector: 'app-ai-stats',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ai-stats.html',
  styleUrl: './ai-stats.scss'
})
export class AIStatsComponent implements OnInit {

  health: AIHealth | null = null;
  stats: AIStats | null = null;
  statsLoading = false;
  retraining = false;

  constructor(private aiService: AIService) {}

  ngOnInit(): void {
    this.loadHealth();
    this.loadStats();
  }

  loadHealth(): void {
    this.aiService.getHealth().subscribe({
      next: h => this.health = h,
      error: () => this.health = null
    });
  }

  loadStats(): void {
    this.statsLoading = true;
    this.aiService.getStats().subscribe({
      next: s => { this.stats = s; this.statsLoading = false; },
      error: () => this.statsLoading = false
    });
  }

  retrain(): void {
    this.retraining = true;
    this.aiService.retrain().subscribe({
      next: () => { this.retraining = false; this.loadStats(); this.loadHealth(); },
      error: () => this.retraining = false
    });
  }
}
