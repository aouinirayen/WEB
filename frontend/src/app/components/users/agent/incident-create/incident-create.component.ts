import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { IncidentNotificationService } from '../../../../services/incident-notification/incident-notification.service';
import { IncidentSummary } from '../../../../models/incident-notification.model';

@Component({
  selector: 'app-incident-create',
  templateUrl: './incident-create.component.html',
  styleUrls: ['./incident-create.component.css']
})
export class IncidentCreateComponent {
  isSubmitting = false;
  feedbackMessage = '';
  feedbackType: 'success' | 'error' = 'success';
  readonly incidentForm;

  // AI analysis result displayed after submission
  aiResult: IncidentSummary | null = null;

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private incidentNotificationService: IncidentNotificationService
  ) {
    this.incidentForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      location: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(500)]]
    });
  }

  submitIncident(): void {
    if (this.incidentForm.invalid) {
      this.incidentForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;
    this.aiResult = null;
    const payload = this.incidentForm.getRawValue() as {
      title: string;
      location: string;
      description: string;
    };

    this.incidentNotificationService
      .createIncident(payload)
      .pipe(finalize(() => (this.isSubmitting = false)))
      .subscribe({
      next: (result) => {
        const safeResult = (result ?? {}) as Partial<IncidentSummary> & { data?: IncidentSummary };
        console.log('Backend API Response:', safeResult);
        console.log('Response keys:', Object.keys(safeResult));
        console.log('Severity:', safeResult.severity);
        console.log('Estimated Delay:', safeResult.estimatedDelayMinutes);
        console.log('Incident Type:', safeResult.incidentType);
        console.log('Confidence:', safeResult.confidencePercent);
        console.log('Agent Message:', safeResult.agentMessage);
        console.log('Passenger Message:', safeResult.passengerMessage);

        // Handle potential wrapping (data might come in result.data or similar)
        let finalResult: Partial<IncidentSummary> = safeResult;
        if (!safeResult.severity && safeResult.data) {
          console.warn('Response was wrapped in .data, unwrapping...');
          finalResult = safeResult.data as IncidentSummary;
          console.log('Unwrapped result:', finalResult);
        }

        // Ensure critical AI fields are always visible even if backend omits some keys.
        this.aiResult = {
          ...finalResult,
          severity: finalResult.severity || 'LOW',
          estimatedDelayMinutes:
            finalResult.estimatedDelayMinutes != null ? finalResult.estimatedDelayMinutes : 5,
          incidentType: finalResult.incidentType || 'general',
          title: finalResult.title || payload.title,
          location: finalResult.location || payload.location,
          reportedByName: finalResult.reportedByName || 'N/A'
        };

        console.log('aiResult assigned:', this.aiResult);
        console.log('aiResult is truthy:', !!this.aiResult);
        console.log('aiResult has severity:', !!this.aiResult?.severity);
        console.log('aiResult has estimatedDelayMinutes:', this.aiResult?.estimatedDelayMinutes);

        this.feedbackMessage = 'Incident submitted successfully! AI has analyzed and classified it.';
        this.feedbackType = 'success';
        this.incidentForm.reset();
      },
      error: (error) => {
        console.error('API Error:', error);
        console.error('Error status:', error.status);
        console.error('Error message:', error.message);
        console.error('Error details:', error);
        this.feedbackMessage = 'Could not submit the incident. Check constraints and retry.';
        this.feedbackType = 'error';
      }
    });
  }

  hasError(controlName: 'title' | 'location' | 'description', error: string): boolean {
    const control = this.incidentForm.get(controlName);
    return !!control && control.touched && control.hasError(error);
  }

  goToIncidentsList(): void {
    this.router.navigate(['/incidents/list']);
  }

  dismissAiResult(): void {
    this.aiResult = null;
  }
}
