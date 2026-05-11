export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'READ';

export interface AppUserRef {
  id: number;
  username: string;
  name?: string;
  email?: string;
  role?: string;
}

export interface AppNotification {
  id: number;
  title: string;
  message: string;
  status: NotificationStatus;
  createdAt: string;
  readAt?: string | null;
  user: AppUserRef;
}

export interface IncidentPayload {
  title: string;
  description: string;
  location: string;
}

export interface IncidentSummary {
  id?: number;
  title: string;
  severity: string;
  location: string;
  reportedByName: string;
  // AI-generated fields
  agentMessage?: string;
  passengerMessage?: string;
  estimatedDelayMinutes?: number;
  incidentType?: string;
  confidencePercent?: number;
}
