export interface Vehicle {
    id?: number;
    plateNumber: string;
    brand: string;
    capacity: number;
    mileage: number;
    type: 'BUS'|'METRO'|'TRAIN'|'LOUAGE'|'BATAH';
    status: 'ACTIVE'|'INACTIVE'|'IN_MAINTENANCE';
    purchaseDate: string;
  }
  
  export interface MaintenanceOrder {
    id?: number;
    vehicle: Vehicle;
    type: 'PREVENTIVE'|'CORRECTIVE';
    status: 'PENDING'|'IN_PROGRESS'|'DONE';
    scheduledDate: string;
    completedDate?: string;
    cost?: number;
    description: string;
    technicianName: string;
  }
  
  export interface Line {
    id?: number;
    name: string;
    code: string;
    mode: 'BUS'|'METRO'|'TRAIN'|'LOUAGE'|'BATAH';
    status: 'ACTIVE'|'INACTIVE'|'SUSPENDED';
  }
  
  export interface Stop {
    id?: number;
    name: string;
    sequence: number;
    latitude: number;
    longitude: number;
    line: Line;
  }
  
  export interface Schedule {
    id?: number;
    line: Line;
    vehicle: Vehicle;
    dayType: 'WEEKDAY'|'WEEKEND'|'HOLIDAY';
    startTime: string;
    endTime: string;
    frequencyMinutes: number;
  }
  
  export interface Trip {
    id?: number;
    schedule: Schedule;
    departureTime: string;
    arrivalTime: string;
    delayMinutes: number;
    completed: boolean;
  }
  
  export interface DashboardStats {
    totalVehicles: number;
    activeVehicles: number;
    totalLines: number;
    activeLines: number;
    pendingMaintenance: number;
    totalTrips: number;
  }
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
  