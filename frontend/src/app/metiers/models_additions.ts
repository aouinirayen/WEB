// ── PASTE THESE INTO YOUR EXISTING models.ts ──────────────────────────────────

export interface Driver {
  id?: number;
  firstName: string;
  lastName: string;
  licenseNumber: string;
  licenseType: 'BUS' | 'METRO' | 'TRAIN' | 'LOUAGE' | 'BATAH';
  phone: string;
  status: 'AVAILABLE' | 'ON_DUTY' | 'OFF_DUTY' | 'SUSPENDED';
  hireDate: string;
}

export interface DriverAssignment {
  id?: number;
  driver: Driver;
  vehicle: Vehicle;
  trip: Trip;
  assignedDate: string;
  notes?: string;
}

export interface IncidentReport {
  id?: number;
  vehicle: Vehicle;
  trip?: Trip;
  line?: Line;
  type: 'BREAKDOWN' | 'ACCIDENT' | 'DELAY' | 'PASSENGER_ISSUE' | 'OTHER';
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status: 'OPEN' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED';
  description: string;
  reportedAt: string;
  resolvedAt?: string;
  reportedBy: string;
}

export interface FuelLog {
  id?: number;
  vehicle: Vehicle;
  liters: number;
  costPerLiter: number;
  totalCost: number;
  mileageAtFillUp: number;
  fuelDate: string;
  station?: string;
  notes?: string;
}
