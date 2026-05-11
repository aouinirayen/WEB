import { Pipe, PipeTransform } from '@angular/core';
import { DocumentStatusEnum } from '../models';

@Pipe({
  name: 'statusLabel'
})
export class StatusLabelPipe implements PipeTransform {
  transform(status: DocumentStatusEnum | string): string {
    if (!status) return '';

    const statusMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: 'Pending Review',
      [DocumentStatusEnum.VALID]: 'Approved',
      [DocumentStatusEnum.REJECTED]: 'Rejected',
      [DocumentStatusEnum.EXPIRED]: 'Expired',
      [DocumentStatusEnum.REQUEST_UPDATE]: 'Update Requested'
    };

    return statusMap[status] || status;
  }
}

@Pipe({
  name: 'statusColor'
})
export class StatusColorPipe implements PipeTransform {
  transform(status: DocumentStatusEnum | string): string {
    if (!status) return 'secondary';

    const colorMap: Record<string, string> = {
      [DocumentStatusEnum.PENDING]: 'warning',
      [DocumentStatusEnum.VALID]: 'success',
      [DocumentStatusEnum.REJECTED]: 'danger',
      [DocumentStatusEnum.EXPIRED]: 'danger',
      [DocumentStatusEnum.REQUEST_UPDATE]: 'info'
    };

    return colorMap[status] || 'secondary';
  }
}

@Pipe({
  name: 'statusHexColor'
})
export class StatusHexColorPipe implements PipeTransform {
  transform(status: DocumentStatusEnum | string): string {
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
}
