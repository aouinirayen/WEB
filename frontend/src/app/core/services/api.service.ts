import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import {
  LoginRequest, AuthResponse, User,
  PricingPlan, SubscriptionRequest, SubscriptionResponse,
  Reduction, LoyaltyAccountResponse, RedeemRequest, PointTransaction,
  PaymentInitRequest, PaymentInitResponse,
  PlanRecommendationResponse, ChurnPredictionResponse, CLVResponse, ActionSendResponse,
  TransportType, PricingType, LoyaltyTier, RoleEnum
} from '../models/models';

interface BackendAuthPayload {
  token: string; id: number; username: string;
  email: string; name: string; role: string; transportType?: string;
}
interface SpringPage<T> { content?: T[]; }
interface AdminUserPayload {
  id: number; username: string; email: string; name: string; role: string;
  cin?: number | null; enabled?: boolean; isEnabled?: boolean;
  createdAt?: string; updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly base = environment.apiBaseUrl.replace(/\/$/, '');
  private readonly mlBase = (environment.mlApiBaseUrl ?? environment.apiBaseUrl).replace(/\/$/, '');
  constructor(private http: HttpClient) {}

  private mapAdminUserToUser(u: AdminUserPayload): User {
    return { id: u.id, username: u.username, email: u.email, name: u.name,
             role: u.role as RoleEnum, isEnabled: u.enabled ?? u.isEnabled ?? true,
             createdAt: u.createdAt };
  }

  private mapPlanFromApi(raw: any): PricingPlan {
    return {
      id: raw?.id,
      nom: raw?.nom,
      description: raw?.description ?? '',
      prix: Number(raw?.prix ?? 0),
      // Backend uses dureeEnMois; frontend UI uses dureeEnJours.
      dureeEnJours: Number(raw?.dureeEnJours ?? raw?.dureeEnMois ?? 0),
      type: raw?.type as PricingType,
      transportType: raw?.transportType,
      createdByUsername: raw?.createdByUsername,
    };
  }

  private mapPlanToApi(plan: PricingPlan): any {
    return {
      nom: plan.nom,
      description: plan.description,
      prix: plan.prix,
      // Backend request DTO expects dureeEnMois.
      dureeEnMois: plan.dureeEnJours,
      type: plan.type,
    };
  }

  private buildCompatiblePaymentPayload(input: {
    passengerId?: number;
    pricingPlanId: number;
    codeReduction?: string;
    autoRenewal?: boolean;
    paymentMode?: 'CASH' | 'POINTS';
    pointsToUse?: number;
  }): any {
    const payload: any = {
      pricingPlanId: input.pricingPlanId,
      planId: input.pricingPlanId
    };

    if (input.passengerId != null) {
      payload.passengerId = input.passengerId;
      payload.userId = input.passengerId;
    }
    if (input.codeReduction && input.codeReduction.trim() !== '') {
      const code = input.codeReduction.trim().toUpperCase();
      payload.codeReduction = code;
      payload.reductionCode = code;
      payload.promoCode = code;
    }
    if (input.autoRenewal === true) {
      payload.autoRenewal = true;
      payload.renewal = true;
    }
    if (input.paymentMode) {
      payload.paymentMode = input.paymentMode;
      payload.mode = input.paymentMode;
    }
    if (input.pointsToUse != null) {
      payload.pointsToUse = input.pointsToUse;
      payload.points = input.pointsToUse;
    }

    return payload;
  }

  // ===== AUTH =====
  login(req: LoginRequest): Observable<AuthResponse> {
    return this.http.post<BackendAuthPayload>(`${this.base}/auth/login`, req).pipe(
      map((res) => ({
        token: res.token,
        user: {
          id: res.id, username: res.username, email: res.email,
          name: res.name, role: res.role as RoleEnum, isEnabled: true,
          ...(res.transportType != null && String(res.transportType).trim() !== ''
            ? { transportType: String(res.transportType).toUpperCase() as TransportType }
            : {}),
        },
      }))
    );
  }

  // ===== USERS =====
  getAllUsers(): Observable<User[]> {
    const params = new HttpParams().set('size', '500').set('page', '0');
    return this.http.get<SpringPage<AdminUserPayload>>(`${this.base}/admin/users`, { params }).pipe(
      map((page) => (page.content ?? []).map((u) => this.mapAdminUserToUser(u)))
    );
  }
  getUserById(id: number): Observable<User> {
    return this.http.get<AdminUserPayload>(`${this.base}/admin/users/${id}`)
      .pipe(map((u) => this.mapAdminUserToUser(u)));
  }
  setTransportType(userId: number, transportType: TransportType): Observable<unknown> {
    return this.http.get<AdminUserPayload>(`${this.base}/admin/users/${userId}`).pipe(
      switchMap((u) => {
        const body = { username: u.username, email: u.email, name: u.name,
                       role: u.role as RoleEnum, ...(u.cin != null ? { cin: u.cin } : {}),
                       enabled: u.enabled ?? u.isEnabled ?? true, transportType };
        return this.http.put(`${this.base}/admin/users/${userId}`, body);
      })
    );
  }

  // ===== PRICING PLANS =====
  getAllPlans(): Observable<PricingPlan[]> {
    return this.http.get<any[]>(`${this.base}/pricing-plans`).pipe(
      map((rows) => (rows ?? []).map((r) => this.mapPlanFromApi(r)))
    );
  }
  getPlansByOperator(operatorId: number): Observable<PricingPlan[]> {
    return this.http.get<any[]>(`${this.base}/pricing-plans/operator/${operatorId}`).pipe(
      map((rows) => (rows ?? []).map((r) => this.mapPlanFromApi(r)))
    );
  }
  getPlanById(id: number): Observable<PricingPlan> {
    return this.http.get<any>(`${this.base}/pricing-plans/${id}`).pipe(
      map((r) => this.mapPlanFromApi(r))
    );
  }
  createPlan(plan: PricingPlan, operatorId: number): Observable<PricingPlan> {
    return this.http.post<any>(`${this.base}/pricing-plans/operator/${operatorId}`, this.mapPlanToApi(plan)).pipe(
      map((r) => this.mapPlanFromApi(r))
    );
  }
  updatePlan(id: number, plan: PricingPlan, operatorId: number): Observable<PricingPlan> {
    return this.http.put<any>(`${this.base}/pricing-plans/${id}/operator/${operatorId}`, this.mapPlanToApi(plan)).pipe(
      map((r) => this.mapPlanFromApi(r))
    );
  }
  deletePlan(id: number, operatorId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/pricing-plans/${id}/operator/${operatorId}`);
  }
  getPlansByTransport(transportType: TransportType): Observable<PricingPlan[]> {
    return this.http.get<PricingPlan[]>(`${this.base}/pricing-plans/by-transport/${transportType}`);
  }
  getPlansByType(type: PricingType): Observable<PricingPlan[]> {
    return this.http.get<PricingPlan[]>(`${this.base}/pricing-plans/by-type/${type}`);
  }

  // ===== SUBSCRIPTIONS =====
  subscribe(req: SubscriptionRequest, passengerId: number): Observable<SubscriptionResponse> {
    const payload: any = {
      pricingPlanId: req.pricingPlanId,
      planId: req.pricingPlanId
    };
    if (req.codeReduction && req.codeReduction.trim() !== '') {
      const code = req.codeReduction.trim().toUpperCase();
      payload.codeReduction = code;
      payload.reductionCode = code;
      payload.promoCode = code;
    }
    return this.http.post<SubscriptionResponse>(`${this.base}/subscriptions/passenger/${passengerId}`, payload);
  }
  getMySubscriptions(passengerId: number): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${this.base}/subscriptions/passenger/${passengerId}`);
  }
  getAllSubscriptions(): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${this.base}/subscriptions`);
  }
  getSubscriptionsByOperator(operatorId: number): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${this.base}/subscriptions/operator/${operatorId}`);
  }
  cancelSubscription(id: number, passengerId: number): Observable<SubscriptionResponse> {
    return this.http.put<SubscriptionResponse>(
      `${this.base}/subscriptions/${id}/cancel/passenger/${passengerId}`, {});
  }

  // ===== REDUCTIONS =====
  getAllReductions(): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.base}/reductions`);
  }
  getReductionsByOperator(operatorId: number): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.base}/reductions/operator/${operatorId}`);
  }
  getValidReductions(): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.base}/reductions/valides`);
  }
  getReductionByCode(code: string): Observable<Reduction> {
    return this.http.get<Reduction>(`${this.base}/reductions/code/${code}`);
  }
  getAccessibleReductions(points: number): Observable<Reduction[]> {
    return this.http.get<Reduction[]>(`${this.base}/reductions/accessibles/${points}`);
  }
  createReduction(r: Reduction, operatorId: number): Observable<Reduction> {
    return this.http.post<Reduction>(`${this.base}/reductions/operator/${operatorId}`, r);
  }
  updateReduction(id: number, r: Reduction): Observable<Reduction> {
    return this.http.put<Reduction>(`${this.base}/reductions/${id}`, r);
  }
  deleteReduction(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/reductions/${id}`);
  }

  // ===== LOYALTY =====
  getMyLoyalty(passengerId: number): Observable<LoyaltyAccountResponse> {
    return this.http.get<LoyaltyAccountResponse>(
      `${this.base}/loyalty-accounts/passenger/${passengerId}`);
  }
  getAllLoyalty(): Observable<LoyaltyAccountResponse[]> {
    return this.http.get<LoyaltyAccountResponse[]>(`${this.base}/loyalty-accounts`);
  }
  getLoyaltyByTier(tier: LoyaltyTier): Observable<LoyaltyAccountResponse[]> {
    return this.http.get<LoyaltyAccountResponse[]>(`${this.base}/loyalty-accounts/by-tier/${tier}`);
  }
  redeemPoints(passengerId: number, req: RedeemRequest): Observable<LoyaltyAccountResponse> {
    return this.http.post<LoyaltyAccountResponse>(
      `${this.base}/loyalty-accounts/redeem/passenger/${passengerId}`, req);
  }
  getTransactions(accountId: number): Observable<PointTransaction[]> {
    return this.http.get<PointTransaction[]>(
      `${this.base}/point-transactions/account/${accountId}`);
  }

  // ===== PAYMENT =====
  initiatePayment(req: PaymentInitRequest): Observable<PaymentInitResponse> {
    return this.http.post<PaymentInitResponse>(
      `${this.base}/payment/initiate`,
      this.buildCompatiblePaymentPayload(req as any)
    );
  }
  initiatePaymentMe(
    pricingPlanId: number, codeReduction?: string, autoRenewal?: boolean,
    paymentMode: 'CASH' | 'POINTS' = 'CASH', pointsToUse?: number,
  ): Observable<PaymentInitResponse> {
    return this.http.post<PaymentInitResponse>(
      `${this.base}/payment/initiate/me`,
      this.buildCompatiblePaymentPayload({
        pricingPlanId,
        codeReduction,
        autoRenewal: autoRenewal === true,
        paymentMode,
        pointsToUse
      })
    );
  }
  updateSubscriptionAutoRenewal(
    subscriptionId: number, passengerId: number, autoRenewal: boolean,
  ): Observable<SubscriptionResponse> {
    return this.http.put<SubscriptionResponse>(
      `${this.base}/subscriptions/${subscriptionId}/auto-renewal/passenger/${passengerId}`,
      { autoRenewal });
  }
  confirmPayment(sessionId: string): Observable<SubscriptionResponse> {
    return this.http.get<SubscriptionResponse>(
      `${this.base}/payment/success?session_id=${sessionId}`);
  }

  // ===== ML =====
  recommendPlan(passengerId: number): Observable<PlanRecommendationResponse> {
    return this.http.get<PlanRecommendationResponse>(`${this.mlBase}/ml/recommend/${passengerId}`);
  }
  predictChurn(passengerId: number): Observable<ChurnPredictionResponse> {
    return this.http.get<ChurnPredictionResponse>(`${this.mlBase}/ml/churn/${passengerId}`);
  }
  predictChurnAll(): Observable<ChurnPredictionResponse[]> {
    return this.http.get<ChurnPredictionResponse[]>(`${this.mlBase}/ml/churn/all`);
  }
  predictCLV(passengerId: number): Observable<CLVResponse> {
    return this.http.get<CLVResponse>(`${this.mlBase}/ml/clv/${passengerId}`);
  }
  sendAction(passengerId: number): Observable<ActionSendResponse> {
    return this.http.post<ActionSendResponse>(`${this.mlBase}/ml/action/send/${passengerId}`, {});
  }
}