import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { map } from 'rxjs/operators';
import {
  AppNotification,
  IncidentPayload,
  IncidentSummary
} from '../../models/incident-notification.model';
import { convertSnakeToCamel } from '../../core/utils/response-mapper';

@Injectable({
  providedIn: 'root'
})
export class IncidentNotificationService {
  private readonly incidentsUrl = '/incidents';
  private readonly notificationsUrl = '/notifications';

  private readonly notificationsSubject = new BehaviorSubject<AppNotification[]>([]);
  readonly notifications$ = this.notificationsSubject.asObservable();

  private readonly unreadCountSubject = new BehaviorSubject<number>(0);
  readonly unreadCount$ = this.unreadCountSubject.asObservable();

  constructor(private http: HttpClient) {}

  private pickFirst<T>(...values: Array<T | null | undefined>): T | undefined {
    return values.find((value) => value !== undefined && value !== null && value !== '') as T | undefined;
  }

  private ensureMailMetrics(
    message: string | undefined,
    severity: string,
    estimatedDelayMinutes: number,
    audience: 'agent' | 'passenger'
  ): string {
    const baseMessage =
      (message ?? '').trim() ||
      (audience === 'agent'
        ? 'Incident reported. Please monitor operations and coordinate actions.'
        : 'A transit incident has been reported. Please plan your trip accordingly.');

    const hasSeverity = /severity\s*[:\-]/i.test(baseMessage);
    const hasDelay = /estimated\s*delay\s*[:\-]/i.test(baseMessage);

    const details: string[] = [];
    if (!hasSeverity) details.push(`Severity: ${severity}`);
    if (!hasDelay) details.push(`Estimated Delay: ${estimatedDelayMinutes} minutes`);

    return details.length ? `${baseMessage}\n${details.join('\n')}` : baseMessage;
  }

  private normalizeIncidentSummary(raw: any): IncidentSummary {
    const converted = convertSnakeToCamel(raw ?? {});
    const ai = converted?.aiAnalysis ?? converted?.analysis ?? converted?.prediction ?? {};
    const data = converted?.data ?? {};

    const severity = this.pickFirst<string>(
      converted?.severity,
      converted?.severityLevel,
      ai?.severity,
      ai?.severityLevel,
      data?.severity,
      data?.severityLevel
    );
    const estimatedDelayMinutesRaw = this.pickFirst<any>(
      converted?.estimatedDelayMinutes,
      converted?.estimatedDelay,
      converted?.delayMinutes,
      converted?.delay,
      ai?.estimatedDelayMinutes,
      ai?.estimatedDelay,
      ai?.delayMinutes,
      ai?.delay,
      data?.estimatedDelay,
      data?.estimatedDelayMinutes
    );
    const incidentType = this.pickFirst<string>(
      converted?.incidentType,
      converted?.type,
      ai?.incidentType,
      ai?.type,
      data?.incidentType
    );
    const confidencePercent = this.pickFirst<number>(
      converted?.confidencePercent,
      converted?.confidence,
      ai?.confidencePercent,
      ai?.confidence,
      data?.confidencePercent
    );
    const agentMessage = this.pickFirst<string>(
      converted?.agentMessage,
      ai?.agentMessage,
      data?.agentMessage
    );
    const passengerMessage = this.pickFirst<string>(
      converted?.passengerMessage,
      ai?.passengerMessage,
      data?.passengerMessage
    );

    const parseDelayFromText = (text: unknown): number | undefined => {
      if (typeof text !== 'string') return undefined;
      const match = text.match(/estimated\s+(?:delay|resolution)\s*[:\-]?\s*(\d+)/i);
      if (!match) return undefined;
      const value = Number(match[1]);
      return Number.isFinite(value) ? value : undefined;
    };

    const inferFromText = (text: string): { severity: string; delay: number; incidentType: string } => {
      const t = (text || '').toLowerCase();

      if (/(fire|flame|smoke|blaze|gas|chemical|toxic|collision|crash|accident|derail|flood|critical|evacuate)/.test(t)) {
        return { severity: 'HIGH', delay: 60, incidentType: 'critical' };
      }
      if (/(delay|broken|medical|injury|collapse|crowd|failure|signal|power|outage|technical|security|threat|fight)/.test(t)) {
        return { severity: 'MEDIUM', delay: 20, incidentType: 'technical' };
      }
      return { severity: 'LOW', delay: 5, incidentType: 'general' };
    };

    const estimatedDelayMinutes = (() => {
      const direct = Number(estimatedDelayMinutesRaw);
      if (Number.isFinite(direct)) return direct;
      return this.pickFirst<number>(
        parseDelayFromText(converted?.agentMessage),
        parseDelayFromText(converted?.passengerMessage),
        parseDelayFromText(ai?.agentMessage),
        parseDelayFromText(ai?.passengerMessage),
        parseDelayFromText(data?.agentMessage),
        parseDelayFromText(data?.passengerMessage)
      );
    })();

    const baseText = [
      converted?.title,
      converted?.description,
      converted?.agentMessage,
      converted?.passengerMessage,
      ai?.agentMessage,
      ai?.passengerMessage,
      data?.title,
      data?.description,
      data?.agentMessage,
      data?.passengerMessage
    ]
      .filter((v) => typeof v === 'string' && String(v).trim() !== '')
      .join(' ');

    const inferred = inferFromText(baseText);

    const normalizedSeverity = severity ?? inferred.severity;
    const normalizedDelay = estimatedDelayMinutes ?? inferred.delay;

    return {
      ...(converted as IncidentSummary),
      ...(data as IncidentSummary),
      severity: normalizedSeverity,
      estimatedDelayMinutes: normalizedDelay,
      incidentType: incidentType ?? inferred.incidentType,
      confidencePercent,
      agentMessage: this.ensureMailMetrics(agentMessage, normalizedSeverity, normalizedDelay, 'agent'),
      passengerMessage: this.ensureMailMetrics(
        passengerMessage,
        normalizedSeverity,
        normalizedDelay,
        'passenger'
      )
    };
  }

  getAllIncidents(): Observable<IncidentSummary[]> {
    return this.http.get<IncidentSummary[]>(this.incidentsUrl).pipe(
      map(incidents => incidents.map(incident => this.normalizeIncidentSummary(incident)))
    );
  }

  createIncident(payload: IncidentPayload): Observable<IncidentSummary> {
    return this.http.post<IncidentSummary>(`${this.incidentsUrl}/add`, payload).pipe(
      map(response => {
        console.log('🔄 Raw response from backend:', response);
        const normalized = this.normalizeIncidentSummary(response);
        console.log('✅ Normalized response:', normalized);
        return normalized;
      })
    );
  }

  updateIncident(incidentId: number, payload: IncidentPayload): Observable<IncidentSummary> {
    return this.http.put<IncidentSummary>(`${this.incidentsUrl}/update/${incidentId}`, payload).pipe(
      map(response => this.normalizeIncidentSummary(response))
    );
  }

  getMyNotifications(): Observable<AppNotification[]> {
    return this.http.get<AppNotification[]>(`${this.notificationsUrl}/my`).pipe(
      map(notifications => notifications.map(n => convertSnakeToCamel(n) as AppNotification)),
      tap((notifications) => this.setNotifications(notifications))
    );
  }

  markNotificationAsRead(notificationId: number): Observable<AppNotification> {
    return this.http.patch<AppNotification>(`${this.notificationsUrl}/${notificationId}/read`, {}).pipe(
      map(response => convertSnakeToCamel(response) as AppNotification),
      tap((updated) => {
        const notifications = this.notificationsSubject.value.map((item) =>
          item.id === updated.id ? updated : item
        );
        this.setNotifications(notifications);
      })
    );
  }

  refreshNotifications(): void {
    this.getMyNotifications().subscribe({
      error: () => {
        // Keep UI stable if endpoint is temporarily unavailable.
      }
    });
  }

  private setNotifications(notifications: AppNotification[]): void {
    const sorted = [...notifications].sort(
      (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    );
    this.notificationsSubject.next(sorted);
    this.unreadCountSubject.next(sorted.filter((item) => item.status !== 'READ').length);
  }
}
