import { Component, Input, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  SparePartService, SparePart,
  MaintenancePartUsage, PartSuggestion
} from '../../services/spare-part.service';

@Component({
  selector: 'app-part-usage-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './part-usage-panel.component.html'
})
export class PartUsagePanelComponent implements OnInit, OnChanges {
  /** Pass the maintenance order ID from the parent (maintenance form/detail) */
  @Input() maintenanceOrderId!: number;
  /** Pass the maintenance type so suggestions are auto-loaded */
  @Input() maintenanceType!: string;

  usages: MaintenancePartUsage[] = [];
  allParts: SparePart[]          = [];
  suggestions: PartSuggestion[]  = [];

  newUsage = { sparePartId: 0, quantityUsed: 1 };
  selectedPartCost = 0;
  totalCost        = 0;
  errorMsg         = '';
  warnMsg          = '';

  constructor(private service: SparePartService) {}

  ngOnInit() {
    this.service.getAll().subscribe(p => this.allParts = p);
    if (this.maintenanceOrderId) this.loadUsages();
  }

  ngOnChanges() {
    if (this.maintenanceOrderId) {
      this.loadUsages();
      this.loadSuggestions();
    }
  }

  loadUsages() {
    this.service.getUsageByOrder(this.maintenanceOrderId).subscribe(data => {
      this.usages    = data;
      this.totalCost = data.reduce((s, u) => s + u.totalCost, 0);
    });
  }

  loadSuggestions() {
    this.service.suggest(this.maintenanceOrderId).subscribe(data => {
      this.suggestions = data;
    });
  }

  onPartSelected(partId: number) {
    const part = this.allParts.find(p => p.id === partId);
    this.selectedPartCost = part?.unitCost ?? 0;
    this.warnMsg = '';
    if (part && part.lowStock) {
      this.warnMsg = `⚠️ ${part.name} is low on stock (${part.stockQuantity} remaining).`;
    }
  }

  onQtyChange() {
    const part = this.allParts.find(p => p.id === this.newUsage.sparePartId);
    if (part && this.newUsage.quantityUsed > part.stockQuantity) {
      this.warnMsg = `Quantity exceeds current stock (${part.stockQuantity} available). Stock will be set to 0.`;
    }
  }

  lineTotal(): number {
    return Math.round(this.selectedPartCost * this.newUsage.quantityUsed * 100) / 100;
  }

  selectSuggestion(s: PartSuggestion) {
    this.newUsage.sparePartId  = s.partId;
    this.selectedPartCost      = s.unitCost;
    this.newUsage.quantityUsed = 1;
    this.warnMsg = s.lowStock
      ? `⚠️ ${s.name} is low on stock (${s.stockQuantity} remaining).` : '';
  }

  addUsage() {
    this.errorMsg = '';
    this.service.recordUsage({
      maintenanceOrderId: this.maintenanceOrderId,
      sparePartId:        this.newUsage.sparePartId,
      quantityUsed:       this.newUsage.quantityUsed,
      usedDate:           new Date().toISOString().split('T')[0]
    }).subscribe({
      next: () => {
        this.newUsage          = { sparePartId: 0, quantityUsed: 1 };
        this.selectedPartCost  = 0;
        this.warnMsg           = '';
        this.loadUsages();
        this.service.getAll().subscribe(p => this.allParts = p); // refresh stock
      },
      error: err => this.errorMsg = err.error?.error || 'Failed to record usage.'
    });
  }

  deleteUsage(id: number) {
    if (confirm('Remove this part usage? Stock will be restored.')) {
      this.service.deleteUsage(id).subscribe(() => {
        this.loadUsages();
        this.service.getAll().subscribe(p => this.allParts = p);
      });
    }
  }
}
