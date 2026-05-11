import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

export type ToastType = 'success' | 'error' | 'warning' | 'info';

export interface ToastNotification {
  id: number;
  type: ToastType;
  title: string;
  message: string;
  createdAt: number;
}

@Injectable({
  providedIn: 'root'
})
export class ToastService {
  private readonly _toasts = new BehaviorSubject<ToastNotification[]>([]);
  public readonly toasts$: Observable<ToastNotification[]> = this._toasts.asObservable();
  private nextId = 1;

  show(type: ToastType, title: string, message: string): void {
    const toast: ToastNotification = {
      id: this.nextId++,
      type,
      title,
      message,
      createdAt: Date.now()
    };

    this._toasts.next([...this._toasts.value, toast]);
    setTimeout(() => this.dismiss(toast.id), 3000);
  }

  success(title: string, message: string): void {
    this.show('success', title, message);
  }

  error(title: string, message: string): void {
    this.show('error', title, message);
  }

  warning(title: string, message: string): void {
    this.show('warning', title, message);
  }

  info(title: string, message: string): void {
    this.show('info', title, message);
  }

  dismiss(id: number): void {
    this._toasts.next(this._toasts.value.filter(toast => toast.id !== id));
  }
}
