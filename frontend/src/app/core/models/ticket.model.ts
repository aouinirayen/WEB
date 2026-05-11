export interface Ticket {
  id?: number;
  type: string;
  price: number;
  description: string;
  validity: string;
  transportType?: string;
  quantiteDisponible?: number;
  lieuDepart?: string;
  destination?: string;
  heureDepart?: string;
}

export interface TransportType {
  value: string;
  label: string;
}
