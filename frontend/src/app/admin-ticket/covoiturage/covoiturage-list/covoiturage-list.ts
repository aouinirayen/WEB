import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CovoiturageService } from '../../../core/services/covoiturage.service';
import { Covoiturage, DriverConfiance } from '../../../core/models/covoiturage.model';

@Component({
  selector: 'app-admin-covoiturage-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './covoiturage-list.html',
  styleUrl: './covoiturage-list.scss'
})
export class CovoiturageListComponent implements OnInit {

  covoiturages: Covoiturage[] = [];
  filteredCovoiturages: Covoiturage[] = [];
  pagedCovoiturages: Covoiturage[] = [];
  selectedDriverConfiance: DriverConfiance | null = null;
  loadingConfiance = false;
  showAllAvis = false;
  searchQuery: string = '';
  sortByPoints: boolean = true;

  // Pagination
  currentPage = 1;
  pageSize = 4;
  totalPages = 1;

  constructor(private covoiturageService: CovoiturageService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.covoiturageService.getAll().subscribe({
      next: data => {
        this.covoiturages = data;
        this.filteredCovoiturages = [...data];
        this.applyFilters();
        this.cdr.detectChanges();
      },
      error: err => console.error('Error loading covoiturages', err)
    });
  }

  applyFilters(): void {
    let filtered = [...this.covoiturages];

    // Search by driver name
    if (this.searchQuery) {
      const query = this.searchQuery.toLowerCase();
      filtered = filtered.filter(c =>
        c.driverName.toLowerCase().includes(query)
      );
    }

    // Sort by confirmed covoiturages count if enabled
    if (this.sortByPoints) {
      const driverConfirmedCounts = new Map<string, number>();
      
      // Count confirmed covoiturages for each driver
      this.covoiturages.forEach(c => {
        if (c.status === 'CONFIRMED') {
          const count = driverConfirmedCounts.get(c.driverName) || 0;
          driverConfirmedCounts.set(c.driverName, count + 1);
        }
      });

      filtered = filtered.sort((a, b) => {
        const countA = driverConfirmedCounts.get(a.driverName) || 0;
        const countB = driverConfirmedCounts.get(b.driverName) || 0;
        return countB - countA; // Descending order
      });
    }

    this.filteredCovoiturages = filtered;
    this.totalPages = Math.max(1, Math.ceil(filtered.length / this.pageSize));
    if (this.currentPage > this.totalPages) this.currentPage = 1;
    this.updatePage();
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.pagedCovoiturages = this.filteredCovoiturages.slice(start, start + this.pageSize);
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

  onSearchChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.searchQuery = input.value;
    this.applyFilters();
  }

  toggleSortByPoints(): void {
    this.sortByPoints = !this.sortByPoints;
    this.applyFilters();
  }

  confirmCov(c: Covoiturage): void {
    this.covoiturageService.confirmer(c.id!).subscribe({
      next: (confiance) => {
        c.status = 'CONFIRMED';
        this.selectedDriverConfiance = confiance;
        this.load();
        // Reload confiance data to ensure it's up to date
        setTimeout(() => {
          this.selectDriverConfiance(c.driverName);
        }, 500);
      },
      error: err => console.error('Error confirming covoiturage', err)
    });
  }

  rejectCov(c: Covoiturage): void {
    c.status = 'REJECTED';
    this.covoiturageService.update(c.id!, c).subscribe({
      next: () => this.load(),
      error: err => console.error('Error rejecting covoiturage', err)
    });
  }

  delete(id: number): void {
    if (window.confirm('Delete this carpool?')) {
      this.covoiturageService.delete(id).subscribe(() => this.load());
    }
  }

  // Confiance methods
  selectDriverConfiance(driverName: string): void {
    this.loadingConfiance = true;
    this.showAllAvis = false;
    this.covoiturageService.getConfiance(driverName).subscribe({
      next: (confiance) => {
        this.selectedDriverConfiance = confiance;
        this.loadingConfiance = false;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error('Error loading confiance', err);
        this.loadingConfiance = false;
      }
    });
  }

  closeConfiancePanel(): void {
    this.selectedDriverConfiance = null;
  }

  toggleAvisFilter(): void {
    this.showAllAvis = !this.showAllAvis;
  }

  seedTestData(): void {
    this.covoiturageService.seedTestConfiance().subscribe({
      next: (message) => {
        alert(message);
        // Reload covoiturages to show the new data
        this.load();
        // Also load confiance for the test driver
        setTimeout(() => {
          this.selectDriverConfiance('Fatma Trabelsi');
        }, 500);
      },
      error: err => console.error('Error seeding test data', err)
    });
  }

  getConfianceColor(points: number): string {
    if (points >= 80) return '#2e7d32';
    if (points >= 60) return '#1976d2';
    if (points >= 40) return '#f57c00';
    return '#c62828';
  }

  formatDate(dateStr: string): string {
    if (!dateStr) return '';
    return dateStr;
  }
}
