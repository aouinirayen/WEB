export interface Covoiturage {
  id?: number;
  driverName: string;
  departure: string;
  destination: string;
  date: string;
  heureDepart: string;
  heureArrivee: string;
  price: number;
  availableSeats: number;
  vehicle: string;
  status?: string;
  departureLat?: number;
  departureLng?: number;
  destinationLat?: number;
  destinationLng?: number;
}

export interface DriverConfiance {
  driverName: string;
  pointsConfiance: number;
  conducteurDeConfiance: boolean;
  autoConfirmation: boolean;
  detailPoints: DetailPoints;
  nombreCovoituragesConfirmes: number;
  seuilCovoiturages: number;
  conditionCovoiturages: boolean;
  nombreAvis: number;
  avisManuel: number;
  avisIA: number;
  seuilAvis: number;
  conditionAvis: boolean;
  moyenneEtoiles: number;
  moyenneManuel: number;
  moyenneIA: number;
  seuilMoyenneEtoiles: number;
  conditionMoyenne: boolean;
  avisList: AvisInfo[];
}

export interface DetailPoints {
  covoiturages: number;
  avis: number;
  etoiles: number;
}

export interface AvisList {
  avisList: AvisInfo[];
}

export interface AvisInfo {
  id: number;
  stars: number;
  source: string;
  route: string;
  createdAt: string;
}
