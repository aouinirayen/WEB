import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface Notification {
  id: number;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private subject = new Subject<Notification>();
  private counter = 0;

  getNotifications(): Observable<Notification> { return this.subject.asObservable(); }

  success(message: string) { this.emit(message, 'success'); }
  error(message: string)   { this.emit(message, 'error'); }
  info(message: string)    { this.emit(message, 'info'); }
  warning(message: string) { this.emit(message, 'warning'); }

  private emit(message: string, type: Notification['type']) {
    this.subject.next({ id: ++this.counter, message, type });
  }
}
