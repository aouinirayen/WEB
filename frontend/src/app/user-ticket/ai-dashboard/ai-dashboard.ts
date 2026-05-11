import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AIService,
  MatchRequest, MatchResult,
  CancellationRequest, CancellationResponse,
  SatisfactionRequest, SatisfactionResponse,
  AIStats, AIHealth,
  CovoiturageOption, DriverRatingInfo
} from '../../core/services/ai.service';
import { ReservationService } from '../../core/services/reservation.service';
import { AuthService } from '../../services/auth/auth.service';

@Component({
  selector: 'app-ai-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-dashboard.html',
  styleUrl: './ai-dashboard.scss'
})
export class AIDashboardComponent implements OnInit {

  Math = Math;
  activeTab: 'matching' | 'cancellation' | 'satisfaction' | 'stats' = 'matching';

  // Health
  health: AIHealth | null = null;

  // Villes tunisiennes avec GPS
  cities: { name: string; lat: number; lng: number }[] = [
    { name: 'Tunis', lat: 36.8065, lng: 10.1815 },
    { name: 'Ariana', lat: 36.8625, lng: 10.1956 },
    { name: 'Ben Arous', lat: 36.7533, lng: 10.2283 },
    { name: 'Manouba', lat: 36.8101, lng: 10.0956 },
    { name: 'Nabeul', lat: 36.4513, lng: 10.7357 },
    { name: 'Sousse', lat: 35.8245, lng: 10.6346 },
    { name: 'Monastir', lat: 35.7643, lng: 10.8113 },
    { name: 'Sfax', lat: 34.7398, lng: 10.7600 },
    { name: 'Kairouan', lat: 35.6781, lng: 10.0963 },
    { name: 'Bizerte', lat: 37.2744, lng: 9.8739 },
    { name: 'Gabes', lat: 33.8815, lng: 10.0982 },
    { name: 'Medenine', lat: 33.3540, lng: 10.5055 },
    { name: 'Zaghouan', lat: 36.4029, lng: 10.1429 },
    { name: 'Beja', lat: 36.7256, lng: 9.1817 },
    { name: 'Jendouba', lat: 36.5012, lng: 8.7802 },
    { name: 'Le Kef', lat: 36.1680, lng: 8.7096 },
    { name: 'Kasserine', lat: 35.1672, lng: 8.8365 },
    { name: 'Gafsa', lat: 34.4250, lng: 8.7842 },
    { name: 'Tozeur', lat: 33.9197, lng: 8.1336 },
    { name: 'Mahdia', lat: 35.5047, lng: 11.0622 },
    { name: 'Siliana', lat: 36.0849, lng: 9.3708 },
    { name: 'Sidi Bouzid', lat: 35.0382, lng: 9.4849 },
    { name: 'Tataouine', lat: 32.9297, lng: 10.4518 },
    { name: 'Kebili', lat: 33.7072, lng: 8.9653 },
  ];

  selectedDeparture = 'Tunis';
  selectedDestination = 'Sousse';

  // Matching
  matchRequest: MatchRequest = {
    passengerLat: 36.8065, passengerLng: 10.1815,
    destLat: 35.8245, destLng: 10.6346,
    heureVoulue: '08:00', budgetMax: 25
  };
  matchResults: MatchResult[] = [];
  matchLoading = false;

  // Cancellation — champs user-friendly
  cancelDeparture = 'Tunis';
  cancelDestination = 'Sousse';
  cancelPrix = 15;
  cancelDate = '';
  cancelHeure = '08:00';
  cancelNbPlaces = 2;
  cancelRequest: CancellationRequest = {
    prix: 15, distanceKm: 100, joursAvant: 3, heure: 8, nbPlaces: 2
  };
  cancelResponse: CancellationResponse | null = null;
  cancelLoading = false;
  cancelDistanceDisplay = 0;

  // Satisfaction — champs user-friendly
  covoiturageOptions: CovoiturageOption[] = [];
  selectedCovoiturageId: number | null = null;
  ratingClientName = '';
  satTrajetQuality = 'bon';
  satPrixFeeling = 'correct';
  satPonctualite = 'a_lheure';
  satRemplissage = 'bien_remplie';
  satDetour = 'non';
  satRequest: SatisfactionRequest = {
    covoiturageId: null, matchScore: 0.8, prixRatio: 1.0, ponctualite: 0.9, placesRatio: 0.5, detourKm: 5
  };
  satResponse: SatisfactionResponse | null = null;
  satLoading = false;
  driverRatings: DriverRatingInfo[] = [];
  ratingBlocked = false;
  ratingBlockMessage = '';

  // Stats
  stats: AIStats | null = null;
  statsLoading = false;
  retraining = false;

  constructor(private aiService: AIService, private reservationService: ReservationService, private authService: AuthService) {}

  ngOnInit(): void {
    this.loadHealth();
  }

  loadHealth(): void {
    this.aiService.getHealth().subscribe({
      next: h => this.health = h,
      error: () => this.health = null
    });
  }

  // --- Matching ---
  onCityChange(): void {
    const dep = this.cities.find(c => c.name === this.selectedDeparture);
    const dest = this.cities.find(c => c.name === this.selectedDestination);
    if (dep) { this.matchRequest.passengerLat = dep.lat; this.matchRequest.passengerLng = dep.lng; }
    if (dest) { this.matchRequest.destLat = dest.lat; this.matchRequest.destLng = dest.lng; }
  }

  findMatches(): void {
    this.onCityChange();
    this.matchLoading = true;
    this.matchResults = [];
    this.aiService.findMatches(this.matchRequest).subscribe({
      next: res => { this.matchResults = res; this.matchLoading = false; },
      error: () => this.matchLoading = false
    });
  }

  // --- Cancellation ---
  onCancelCityChange(): void {
    const dep = this.cities.find(c => c.name === this.cancelDeparture);
    const dest = this.cities.find(c => c.name === this.cancelDestination);
    if (dep && dest) {
      const R = 6371;
      const dLat = (dest.lat - dep.lat) * Math.PI / 180;
      const dLng = (dest.lng - dep.lng) * Math.PI / 180;
      const a = Math.sin(dLat/2)**2 + Math.cos(dep.lat*Math.PI/180)*Math.cos(dest.lat*Math.PI/180)*Math.sin(dLng/2)**2;
      this.cancelDistanceDisplay = Math.round(R * 2 * Math.asin(Math.sqrt(a)));
    }
  }

  buildCancelRequest(): void {
    this.onCancelCityChange();
    this.cancelRequest.prix = this.cancelPrix;
    this.cancelRequest.distanceKm = this.cancelDistanceDisplay;
    this.cancelRequest.nbPlaces = this.cancelNbPlaces;
    // Calculer jours avant depart
    if (this.cancelDate) {
      const diff = (new Date(this.cancelDate).getTime() - new Date().getTime()) / (1000*60*60*24);
      this.cancelRequest.joursAvant = Math.max(0, Math.round(diff));
    } else {
      this.cancelRequest.joursAvant = 3;
    }
    // Heure
    if (this.cancelHeure) {
      this.cancelRequest.heure = parseInt(this.cancelHeure.split(':')[0], 10);
    }
  }

  predictCancellation(): void {
    this.buildCancelRequest();
    this.cancelLoading = true;
    this.cancelResponse = null;
    this.aiService.predictCancellation(this.cancelRequest).subscribe({
      next: res => { this.cancelResponse = res; this.cancelLoading = false; },
      error: () => this.cancelLoading = false
    });
  }

  // --- Satisfaction ---
  loadCovoiturages(): void {
    const user = this.authService.currentUserValue;
    if (user && user.name) {
      this.ratingClientName = user.name;
      this.reservationService.getRatableCovoiturages(user.name).subscribe({
        next: list => {
          this.covoiturageOptions = list;
          // Auto-enable rating since we already filtered to completed covoiturages
          this.ratingBlocked = list.length === 0;
          this.ratingBlockMessage = list.length === 0
            ? 'No completed carpool found. Book and confirm a trip to be able to rate.'
            : '';
        },
        error: () => {
          this.covoiturageOptions = [];
          this.ratingBlocked = true;
          this.ratingBlockMessage = 'Error loading your carpools.';
        }
      });
    } else {
      this.ratingBlocked = true;
      this.ratingBlockMessage = 'Please log in to access your carpools.';
    }
  }

  loadDriverRatings(): void {
    this.aiService.getDriverRatings().subscribe({
      next: r => this.driverRatings = r,
      error: () => this.driverRatings = []
    });
  }

  buildSatRequest(): void {
    const qualityMap: Record<string,number> = { excellent: 0.95, bon: 0.75, moyen: 0.5, mauvais: 0.2 };
    const prixMap: Record<string,number> = { tres_bon_marche: 0.5, correct: 1.0, un_peu_cher: 1.3, trop_cher: 2.0 };
    const ponctMap: Record<string,number> = { tres_ponctuel: 1.0, a_lheure: 0.85, un_peu_en_retard: 0.5, tres_en_retard: 0.1 };
    const rempMap: Record<string,number> = { vide: 0.1, peu_remplie: 0.3, bien_remplie: 0.6, pleine: 1.0 };
    const detourMap: Record<string,number> = { non: 0, petit: 5, moyen: 15, grand: 30 };
    this.satRequest.matchScore = qualityMap[this.satTrajetQuality] ?? 0.75;
    this.satRequest.prixRatio = prixMap[this.satPrixFeeling] ?? 1.0;
    this.satRequest.ponctualite = ponctMap[this.satPonctualite] ?? 0.85;
    this.satRequest.placesRatio = rempMap[this.satRemplissage] ?? 0.5;
    this.satRequest.detourKm = detourMap[this.satDetour] ?? 0;
    this.satRequest.covoiturageId = this.selectedCovoiturageId;
  }

  checkRatingEligibility(): void {
    // Since we already loaded only ratable covoiturages, just verify one is selected
    if (!this.selectedCovoiturageId) {
      this.ratingBlocked = true;
      this.ratingBlockMessage = 'Please select a carpool.';
      return;
    }
    this.ratingBlocked = false;
    this.ratingBlockMessage = '';
  }

  predictSatisfaction(): void {
    if (this.ratingBlocked) return;
    this.buildSatRequest();
    this.satLoading = true;
    this.satResponse = null;
    this.aiService.predictSatisfaction(this.satRequest).subscribe({
      next: res => { this.satResponse = res; this.satLoading = false; this.loadDriverRatings(); },
      error: () => this.satLoading = false
    });
  }

  // --- Stats ---
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

  getRiskColor(level: string): string {
    switch (level) {
      case 'FAIBLE': return '#28a745';
      case 'MOYEN': return '#ffc107';
      case 'ELEVE': return '#dc3545';
      default: return '#6c757d';
    }
  }

  getStars(count: number): string {
    return '★'.repeat(count) + '☆'.repeat(5 - count);
  }

  getScoreColor(score: number): string {
    if (score >= 0.7) return '#28a745';
    if (score >= 0.4) return '#ffc107';
    return '#dc3545';
  }

  switchTab(tab: 'matching' | 'cancellation' | 'satisfaction' | 'stats'): void {
    this.activeTab = tab;
    if (tab === 'stats') this.loadStats();
    if (tab === 'satisfaction') { this.loadCovoiturages(); this.loadDriverRatings(); }
  }
}
