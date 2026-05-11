export interface Organization {
  id?: number;
  name: string;
  acronyme: string;
  transportType: string;
  email: string;
  phoneNumber: string;
  website?: string;
  logo?: string;
  type: 'PUBLIC' | 'PRIVATE' | 'MIXED';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED';
  coverageType: 'BUS' | 'TRAIN' | 'METRO' | 'LOUAGE' | 'BATAH';
}
