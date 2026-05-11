import { ChangeDetectionStrategy, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ToastNotification, ToastService } from '../../../services/shared/toast.service';

@Component({
  selector: 'app-toast-notification',
  templateUrl: './toast-notification.component.html',
  styleUrls: ['./toast-notification.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ToastNotificationComponent implements OnInit, OnDestroy {
  toasts: ToastNotification[] = [];
  private subscription?: Subscription;

  constructor(private toastService: ToastService) {}

  ngOnInit(): void {
    this.subscription = this.toastService.toasts$.subscribe(items => {
      this.toasts = items;
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }

  dismiss(toastId: number): void {
    this.toastService.dismiss(toastId);
  }
}
