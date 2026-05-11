export interface Partner {
  id?: number;
  name: string;
  industrySector: string;
  partnershipType: string;
  email: string;
  phoneNumber: string;
  website?: string;
  logo?: string;
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'TERMINATED';
  organizationId?: number | null;
}