export interface Vehicle {
    id?: number;
    plateNumber: string;
    brand: string;
    capacity: number;
    mileage: number;
    type: 'BUS' | 'METRO' | 'TRAIN' | 'LOUAGE' | 'BATAH';
    status: 'ACTIVE' | 'INACTIVE' | 'IN_MAINTENANCE';
    purchaseDate: string;
  }
  
  export interface MaintenanceOrder {
    id?: number;
    vehicle: Vehicle;
    type: 'PREVENTIVE' | 'CORRECTIVE';
    status: 'PENDING' | 'IN_PROGRESS' | 'DONE';
    scheduledDate: string;
    completedDate?: string;
    cost?: number;
    description: string;
    technicianName: string;
  }