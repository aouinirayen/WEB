export interface Reservation {
  id?: number;
  clientName: string;
  phone: string;
  email?: string;
  seatsReserved: number;
  bookingDate: string;
  status: string;
  covoiturageId?: number;
  covoiturageInfo?: string;
  clientLat?: number;
  clientLng?: number;
  clientAddress?: string;
  displacementRequested?: boolean;
  displacementPrice?: number;
}
