import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule, DatePipe } from '@angular/common';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

// Composants collegue
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/authentication/login/login.component';
import { RegisterComponent } from './components/authentication/register/register.component';
import { ForgotPasswordComponent } from './components/authentication/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './components/authentication/reset-password/reset-password.component';
import { AccessDeniedComponent } from './components/access-denied/access-denied.component';
import { AdminDhasbordComponent } from './components/admin/admin-dhasbord.component';
import { AgentDhasbordComponent } from './components/users/agent/agent-dhasbord/agent-dhasbord.component';
import { OperatorDhasbordComponent } from './components/users/operator/operator-dhasbord/operator-dhasbord.component';
import { PassengerDhasbordComponent } from './components/users/passenger/passenger-dhasbord/passenger-dhasbord.component';
import { AdminUserManagementComponent } from './components/admin/users/admin-user-management.component';
import { UserModalComponent } from './components/shared/user-modal/user-modal.component';
import { ConfirmationModalComponent } from './components/shared/confirmation-modal/confirmation-modal.component';
import { ToastNotificationComponent } from './components/shared/toast-notification/toast-notification.component';
import { HeaderComponent } from './components/shared/header/header.component';
import { FooterComponent } from './components/shared/footer/footer.component';
import { ProfileComponent } from './components/users/profile/profile.component';
import { NotificationListComponent } from './components/users/notifications/notification-list/notification-list.component';
import { IncidentManagementComponent } from './components/users/agent/incident-management/incident-management.component';
import { IncidentCreateComponent } from './components/users/agent/incident-create/incident-create.component';

import { BackofficeSidebarComponent } from './components/shared/backoffice-sidebar/backoffice-sidebar.component';
import { FrontofficeLayoutComponent } from './components/shared/frontoffice-layout/frontoffice-layout.component';
import { BackofficeLayoutComponent } from './components/shared/backoffice-layout/backoffice-layout.component';

// Shared Components
import { PaginationComponent } from './components/shared/pagination/pagination.component';
// StatusBadgeComponent is now standalone

// Pipes
import { StatusLabelPipe, StatusColorPipe, StatusHexColorPipe } from './pipes/status.pipe';

// MES composants (Rayen)
import { OrganizationListComponent } from './components/admin/organization/organization-list/organization-list.component';
import { OrganizationFormComponent } from './components/admin/organization/organization-form/organization-form.component';
import { OrganizationDetailComponent } from './components/admin/organization/organization-detail/organization-detail.component';
import { PartnerListComponent as AdminPartnerListComponent } from './components/admin/partner/partner-list/partner-list.component';
import { PartnerFormComponent } from './components/admin/partner/partner-form/partner-form.component';
import { ContractListComponent } from './components/admin/contract/contract-list/contract-list.component';
import { ContractFormComponent } from './components/admin/contract/contract-form/contract-form.component';
import { OperatorListComponent } from './components/users/operator-partner/operator-list/operator-list.component';
import { OperatorDetailComponent } from './components/users/operator-partner/operator-detail/operator-detail.component';
import { PartnerListComponent as UserPartnerListComponent } from './components/users/operator-partner/partner-list/partner-list.component';
import { MapComponent } from './components/shared/map/map.component';
import { ContractRemindersComponent } from './components/admin/contract-reminders/contract-reminders.component';
import { MapPickerComponent } from './components/shared/map-picker/map-picker.component';
import { DashboardComponent } from './components/admin/dashboard/dashboard.component';

// Services & Guards
import { AuthService } from './services/auth/auth.service';
import { JwtInterceptor } from './services/auth/jwt.interceptor';
import { AuthGuard } from './guards/auth.guard';

// Feature Modules
import { UserActivityLogModule } from './components/admin/user-activity-log/user-activity-log.module';
import { DocumentExpiryAlertsModule } from './components/admin/document-expiry-alerts/document-expiry-alerts.module';
import { AuditLogModule } from './components/admin/audit-log/audit-log.module';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    AccessDeniedComponent,
    AdminDhasbordComponent,
    AgentDhasbordComponent,
    OperatorDhasbordComponent,
    PassengerDhasbordComponent,
    AdminUserManagementComponent,
    UserModalComponent,
    ConfirmationModalComponent,
    ToastNotificationComponent,
    HeaderComponent,
    FooterComponent,
    ProfileComponent,
    NotificationListComponent,
    IncidentManagementComponent,
    IncidentCreateComponent,
    BackofficeSidebarComponent,
    FrontofficeLayoutComponent,
    BackofficeLayoutComponent,
    // Pipes
    StatusLabelPipe,
    StatusColorPipe,
    StatusHexColorPipe,
    OrganizationListComponent,
    OrganizationFormComponent,
    OrganizationDetailComponent,
    AdminPartnerListComponent,
    PartnerFormComponent,
    ContractListComponent,
    ContractFormComponent,
    OperatorListComponent,
    OperatorDetailComponent,
    UserPartnerListComponent,
    MapComponent,
    MapPickerComponent,
    ContractRemindersComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    RouterModule,
    UserActivityLogModule,
    DocumentExpiryAlertsModule,
    AuditLogModule,
    PaginationComponent
  ],
  providers: [
    AuthService,
    AuthGuard,
    DatePipe,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

