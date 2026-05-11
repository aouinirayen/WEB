import { Component, OnInit } from "@angular/core";
import { HttpClient } from "@angular/common/http";

@Component({
  selector: "app-contract-reminders",
  templateUrl: "./contract-reminders.component.html"
})
export class ContractRemindersComponent implements OnInit {
  data: any = null;
  loading = false;
  triggerLoading = false;
  triggerResult: any = null;
  days = 365;

  // Modals
  showConfirmModal = false;
  showSuccessModal = false;
  showErrorModal = false;
  errorMessage = "";
  successMessage = "";
  emailsSent = 0;

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadExpiringContracts();
  }

  loadExpiringContracts(): void {
    this.loading = true;
    this.http.get<any>(`/api/contracts/reminders/expiring?days=${this.days}`).subscribe({
      next: (data) => { this.data = data; this.loading = false; },
      error: () => this.loading = false
    });
  }

  openConfirmModal(): void {
    this.showConfirmModal = true;
  }

  confirmSendReminders(): void {
    this.showConfirmModal = false;
    this.triggerLoading = true;

    this.http.post<any>("/api/contracts/reminders/trigger", {}).subscribe({
      next: (result) => {
        this.triggerResult = result;
        this.triggerLoading = false;
        this.emailsSent = result?.emailsSent || this.data?.urgent?.length || 0;
        this.successMessage = `${this.emailsSent} reminder email(s) sent successfully to partners`;
        this.showSuccessModal = true;
        // Reload data
        this.loadExpiringContracts();
      },
      error: (err) => {
        this.triggerLoading = false;
        this.errorMessage = err.error?.message || "Failed to send reminder emails. Please check your SMTP configuration.";
        this.showErrorModal = true;
      }
    });
  }

  getUrgencyColor(urgency: string): string {
    return urgency === "HIGH" ? "#d32f2f" : urgency === "MEDIUM" ? "#f57c00" : "#1a73e8";
  }

  getUrgencyBg(urgency: string): string {
    return urgency === "HIGH" ? "#ffebee" : urgency === "MEDIUM" ? "#fff3e0" : "#e3f2fd";
  }
}
