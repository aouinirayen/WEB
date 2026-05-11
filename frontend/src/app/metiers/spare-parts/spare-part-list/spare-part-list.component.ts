import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SparePartService, SparePart } from '../../../services/spare-part.service';
import { PredictionWidgetComponent } from '../../prediction-widget/prediction-widget.component';
@Component({
  selector: 'app-spare-part-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, PredictionWidgetComponent],
  templateUrl: './spare-part-list.component.html'
})
export class SparePartListComponent implements OnInit {
  parts: SparePart[]        = [];
  lowStockParts: SparePart[] = [];

  filterName     = '';
  filterCategory = '';
  filterLowOnly  = false;

  allCategories = ['ENGINE','BRAKES','TIRES','ELECTRICAL','FILTERS',
                   'TRANSMISSION','BODYWORK','HVAC','OTHER'];

  constructor(private service: SparePartService) {}

  ngOnInit() { this.load(); }

  load() {
    this.service.getAll().subscribe(data => {
      this.parts         = data;
      this.lowStockParts = data.filter(p => p.lowStock);
    });
  }

  filtered(): SparePart[] {
    return this.parts.filter(p => {
      const nameOk     = !this.filterName ||
        p.name.toLowerCase().includes(this.filterName.toLowerCase()) ||
        p.referenceCode.toLowerCase().includes(this.filterName.toLowerCase());
      const catOk      = !this.filterCategory || p.category === this.filterCategory;
      const lowOk      = !this.filterLowOnly  || p.lowStock;
      return nameOk && catOk && lowOk;
    });
  }

  totalValue(): number {
    return this.parts.reduce((s, p) => s + p.stockQuantity * p.unitCost, 0);
  }

  categories(): string[] {
    return [...new Set(this.parts.map(p => p.category))];
  }

  clearFilters() {
    this.filterName     = '';
    this.filterCategory = '';
    this.filterLowOnly  = false;
  }

  delete(id: number) {
    if (confirm('Delete this spare part?')) {
      this.service.delete(id).subscribe(() => this.load());
    }
  }
}
