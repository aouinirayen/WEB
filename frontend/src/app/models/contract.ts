export interface Contract {
  id?: number;
  contractType: 'COMMERCIAL' | 'TECHNICAL' | 'INSTITUTIONAL';
  status: 'DRAFT' | 'ACTIVE' | 'EXPIRED' | 'TERMINATED';
  startDate: string;
  endDate: string;
  description?: string;
  organizationId?: number;
  organizationName?: string;
  partnerId?: number;
  partnerName?: string;
}
