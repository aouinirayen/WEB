// ===== ENUMS =====
export enum RoleEnum { ADMIN = 'ADMIN', AGENT = 'AGENT', OPERATOR = 'OPERATOR', PASSENGER = 'PASSENGER' }
export enum PricingType { FREE = 'FREE', BASIC = 'BASIC', PREMIUM = 'PREMIUM' }
export enum SubscriptionStatus { ACTIVE = 'ACTIVE', EXPIRED = 'EXPIRED', CANCELLED = 'CANCELLED' }
export enum LoyaltyTier { BRONZE = 'BRONZE', SILVER = 'SILVER', GOLD = 'GOLD' }
export enum TransactionType { EARNED = 'EARNED', REDEEMED = 'REDEEMED' }
export enum TransportType { BUS = 'BUS', METRO = 'METRO', TRAIN = 'TRAIN', LOUAGE = 'LOUAGE', BATTAH = 'BATTAH' }

// ===== USER =====
export interface User {
  id: number;
  username: string;
  email: string;
  name: string;
  phone?: string;
  role: RoleEnum;
  transportType?: TransportType;
  isEnabled: boolean;
  createdAt?: string;
}

// ===== AUTH =====
export interface LoginRequest { username: string; password: string; }
export interface AuthResponse { token: string; refreshToken?: string; user?: User; }

// ===== PRICING PLAN =====
export interface PricingPlan {
  id?: number;
  nom: string;
  description: string;
  prix: number;
  dureeEnJours: number;
  type: PricingType;
  transportType?: TransportType;
  createdByUsername?: string;
}

// ===== SUBSCRIPTION =====
export interface SubscriptionRequest {
  pricingPlanId: number;
  codeReduction?: string;
}

export interface SubscriptionResponse {
  id: number;
  dateDebut: string;
  dateFin: string;
  statut: SubscriptionStatus;
  passengerId?: number;
  passengerUsername: string;
  passengerName: string;
  pricingPlan: PricingPlan;
  pointsGagnes: number;
  autoRenewal?: boolean;
  stripeCustomerId?: string;
}

// ===== REDUCTION =====
export interface Reduction {
  id?: number;
  code: string;
  pourcentage: number;
  dateExpiration: string;
  pointsRequis: number;
  createdByUsername?: string;
  estValide?: boolean;
}

// ===== LOYALTY =====
export interface LoyaltyAccountResponse {
  id: number;
  pointsCumules: number;
  niveau: LoyaltyTier;
  passengerUsername: string;
  passengerName: string;
  pointsPourProchainNiveau: number;
  messageProgression: string;
}

export interface RedeemRequest {
  reductionId: number;
  pointsAUtiliser: number;
}

export interface PointTransaction {
  id: number;
  points: number;
  type: TransactionType;
  date: string;
  description: string;
}

// ===== PAYMENT =====
export interface PaymentInitRequest {
  passengerId: number;
  pricingPlanId: number;
  codeReduction?: string;
  /** Rappels J-7 / J-1 + intention de renouvellement (backend). */
  autoRenewal?: boolean;
  /** CASH (Stripe) ou POINTS (wallet fidélité) selon backend. */
  paymentMode?: 'CASH' | 'POINTS';
  /** Nombre de points à déduire si paymentMode=POINTS. */
  pointsToUse?: number;
}

export interface PaymentInitResponse {
  checkoutUrl: string;
  sessionId: string;
  montantDT: number;
  planNom: string;
}

// ===== ML =====
export interface PlanRecommendationResponse {
  userId: number;
  username: string;
  recommendedPlan: string;
  confidence: number;
  reason: string;
}

export interface ChurnPredictionResponse {
  userId: number;
  username: string;
  churnProbability: number;
  riskLevel: string;
  suggestedAction: string;
  suggestedPromoCode?: string;
}
export interface CLVResponse {
  passengerId:    number;
  username:       string;
  clvValue:       number;
  currency:       string;
  action:         string;
  interpretation: string;
}

export interface ActionSendResponse {
  passengerId:        number;
  username:           string;
  action:             string;
  promoCode:          string;
  discountPercentage: number;
  riskLevel:          string;
  message:            string;
  emailSent:          boolean;
}
