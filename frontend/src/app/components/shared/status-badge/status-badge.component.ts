import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentStatusEnum } from '../../../models';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span
      class="badge"
      [style.backgroundColor]="'#' + getStatusColor(status).slice(1)"
      [style.color]="getTextColor(status)"
      [title]="getStatusLabel(status)">
      {{ getStatusLabel(status) }}
    </span>
  `,
  styles: [`
    .badge {
      padding: 0.5rem 0.75rem;
      border-radius: 0.25rem;
      font-size: 0.85rem;
      font-weight: 500;
      white-space: nowrap;
      display: inline-block;
    }
  `]
})
export class StatusBadgeComponent {
  @Input() status: DocumentStatusEnum | string = '';

  getStatusLabel(status: DocumentStatusEnum | string): string {
    if (!status) return '';

    const statusMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: 'Pending',
      [DocumentStatusEnum.VALID]: 'Approved',
      [DocumentStatusEnum.REJECTED]: 'Rejected',
      [DocumentStatusEnum.EXPIRED]: 'Expired',
      [DocumentStatusEnum.REQUEST_UPDATE]: 'Update Requested'
    };

    return statusMap[status] || status;
  }

  getStatusColor(status: DocumentStatusEnum | string): string {
    if (!status) return '#6C757D';

    const hexColorMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: '#FFC107',
      [DocumentStatusEnum.VALID]: '#28A745',
      [DocumentStatusEnum.REJECTED]: '#DC3545',
      [DocumentStatusEnum.EXPIRED]: '#FF6B00',
      [DocumentStatusEnum.REQUEST_UPDATE]: '#007BFF'
    };

    return hexColorMap[status] || '#6C757D';
  }

  getTextColor(status: DocumentStatusEnum | string): string {
    // Lighter backgrounds get dark text, darker backgrounds get light text
    const darkBackgrounds = [DocumentStatusEnum.EXPIRED, DocumentStatusEnum.REJECTED, DocumentStatusEnum.REQUEST_UPDATE];
    return darkBackgrounds.includes(status as DocumentStatusEnum) ? '#FFFFFF' : '#000000';
  }
}
