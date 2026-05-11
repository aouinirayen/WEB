import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <nav aria-label="Page navigation" class="pagination-nav">
      <div class="page-info">
        Showing {{ startItem }} to {{ endItem }}
        of {{ totalElements }} results
      </div>
      <ul class="pagination">
        <li class="page-item" [ngClass]="{ disabled: pageNumber === 0 }">
          <button class="page-link" (click)="previousPage()" [disabled]="pageNumber === 0">
            Previous
          </button>
        </li>

        <li class="page-item" *ngFor="let page of getPageNumbers()" [ngClass]="{ active: page === pageNumber }">
          <button class="page-link" (click)="goToPage(page)">{{ page + 1 }}</button>
        </li>

        <li class="page-item" [ngClass]="{ disabled: pageNumber >= totalPages - 1 }">
          <button class="page-link" (click)="nextPage()" [disabled]="pageNumber >= totalPages - 1">
            Next
          </button>
        </li>
      </ul>
      <div class="page-size-selector">
        <label for="pageSize">Per page:</label>
        <select
          id="pageSize"
          class="page-size-select"
          [(ngModel)]="pageSize"
          (change)="onPageSizeChange()">
          <option [value]="5">5</option>
          <option [value]="10">10</option>
          <option [value]="25">25</option>
          <option [value]="50">50</option>
        </select>
      </div>
    </nav>
  `,
  styles: [`
    :host {
      display: block;
      margin-top: 1rem;
    }

    .pagination-nav {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 1rem;
      padding: 1rem;
      background: #f9fafb;
      border-top: 1px solid #e5e7eb;
      border-radius: 0 0 8px 8px;
      flex-wrap: wrap;
    }

    .page-info {
      font-size: 0.88rem;
      color: #374151;
      font-weight: 500;
      min-width: 200px;
    }

    .pagination {
      display: flex;
      align-items: center;
      gap: 4px;
      list-style: none;
      margin: 0;
      padding: 0;
      flex: 1;
      justify-content: center;
    }

    .page-item {
      display: inline-flex;
    }

    .page-item.disabled {
      opacity: 0.5;
      pointer-events: none;
    }

    .page-item.active .page-link {
      background: #1a73e8;
      color: #ffffff;
      border-color: #1a73e8;
      cursor: default;
    }

    .page-link {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      min-width: 70px;
      height: 36px;
      padding: 0 12px;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      background: #ffffff;
      color: #374151;
      font-size: 0.9rem;
      font-weight: 500;
      cursor: pointer;
      transition: all 0.2s ease;
      text-decoration: none;
      white-space: nowrap;
    }

    .page-link:hover:not(:disabled) {
      background: #f3f4f6;
      border-color: #1a73e8;
      color: #1a73e8;
    }

    .page-link:disabled {
      cursor: not-allowed;
      opacity: 0.5;
    }

    .page-size-selector {
      display: flex;
      align-items: center;
      gap: 8px;
      min-width: 150px;
    }

    .page-size-selector label {
      font-size: 0.88rem;
      color: #374151;
      font-weight: 500;
      white-space: nowrap;
    }

    .page-size-select {
      height: 36px;
      padding: 0 10px;
      border: 1px solid #d1d5db;
      border-radius: 6px;
      background: #ffffff;
      font-size: 0.85rem;
      color: #374151;
      font-weight: 500;
      cursor: pointer;
      outline: none;
      transition: all 0.2s ease;
      min-width: 60px;
    }

    .page-size-select:hover {
      border-color: #1a73e8;
    }

    .page-size-select:focus {
      border-color: #1a73e8;
      box-shadow: 0 0 0 3px rgba(26, 115, 232, 0.1);
    }

    @media (max-width: 768px) {
      .pagination-nav {
        flex-direction: column;
        gap: 0.75rem;
      }

      .page-info {
        order: 1;
        width: 100%;
        text-align: center;
      }

      .pagination {
        order: 2;
        width: 100%;
      }

      .page-size-selector {
        order: 3;
        width: 100%;
        justify-content: center;
      }
    }
  `]
})
export class PaginationComponent {
  @Input() pageNumber: number = 0;
  @Input() pageSize: number = 10;
  @Input() totalElements: number = 0;
  @Input() totalPages: number = 1;

  @Output() pageChanged = new EventEmitter<number>();
  @Output() pageSizeChanged = new EventEmitter<number>();

  Math = Math;

  /**
   * Calculate the start item number for the current page
   */
  get startItem(): number {
    // Ensure all values are valid finite numbers
    const pageNum = Number.isFinite(this.pageNumber) ? this.pageNumber : 0;
    const pageSize = Number.isFinite(this.pageSize) ? this.pageSize : 10;
    const totalEls = Number.isFinite(this.totalElements) ? this.totalElements : 0;

    if (totalEls <= 0) {
      return 0;
    }
    return (pageNum * pageSize) + 1;
  }

  /**
   * Calculate the end item number for the current page
   */
  get endItem(): number {
    // Ensure all values are valid finite numbers
    const pageNum = Number.isFinite(this.pageNumber) ? this.pageNumber : 0;
    const pageSize = Number.isFinite(this.pageSize) ? this.pageSize : 10;
    const totalEls = Number.isFinite(this.totalElements) ? this.totalElements : 0;

    if (totalEls <= 0) {
      return 0;
    }
    return Math.min((pageNum + 1) * pageSize, totalEls);
  }

  previousPage(): void {
    if (this.pageNumber > 0) {
      this.pageNumber--;
      this.pageChanged.emit(this.pageNumber);
    }
  }

  nextPage(): void {
    if (this.pageNumber < this.totalPages - 1) {
      this.pageNumber++;
      this.pageChanged.emit(this.pageNumber);
    }
  }

  goToPage(pageNumber: number): void {
    this.pageNumber = pageNumber;
    this.pageChanged.emit(this.pageNumber);
  }

  onPageSizeChange(): void {
    this.pageNumber = 0; // Reset to first page when changing page size
    this.pageSizeChanged.emit(this.pageSize);
  }

  getPageNumbers(): number[] {
    const pages: number[] = [];
    const maxPages = Math.min(5, this.totalPages);
    let startPage = Math.max(0, this.pageNumber - Math.floor(maxPages / 2));
    let endPage = Math.min(this.totalPages, startPage + maxPages);

    // Adjust start page if we're near the end
    if (endPage - startPage < maxPages) {
      startPage = Math.max(0, endPage - maxPages);
    }

    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }

    return pages;
  }
}
