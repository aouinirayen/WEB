import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
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
import { AuditLogComponent } from './components/admin/audit-log/audit-log.component';
import { DocumentExpiryAlertsComponent } from './components/admin/document-expiry-alerts/document-expiry-alerts.component';
import { AuthGuard } from './guards/auth.guard';
import { FrontofficeLayoutComponent } from './components/shared/frontoffice-layout/frontoffice-layout.component';
import { BackofficeLayoutComponent } from './components/shared/backoffice-layout/backoffice-layout.component';

// Colleague features (passenger / operator)
import { PassengerPlansComponent } from './features/passenger/plans/passenger-plans.component';
import { PassengerSubscriptionsComponent } from './features/passenger/subscription/passenger-subscriptions.component';
import { PassengerLoyaltyComponent } from './features/passenger/loyalty/passenger-loyalty.component';
import { PaymentSuccessComponent, PaymentCancelComponent } from './features/passenger/payment/payment.component';

import { PricingPlanComponent } from './features/operator/pricing-plan/pricing-plan.component';
import { ReductionComponent } from './features/operator/reduction/reduction.component';
import { MlDashboardComponent } from './features/operator/ml-dashboard/ml-dashboard.component';
import { OperatorSubscriptionsComponent } from './features/operator/subscriptions/operator-subscriptions.component';
import { OperatorLoyaltyComponent } from './features/operator/loyalty/operator-loyalty.component';

// Document Management Components
import { DocumentListComponent } from './components/users/documents/document-list/document-list.component';
import { DocumentUploadComponent } from './components/users/documents/document-upload/document-upload.component';
import { DocumentDetailComponent } from './components/users/documents/document-detail/document-detail.component';
import { AdminDocumentListComponent } from './components/admin/documents-admin/admin-document-list/admin-document-list.component';
import { DocumentTypeManagerComponent } from './components/admin/documents-admin/document-type-manager/document-type-manager.component';
import { ProfileComponent } from './components/users/profile/profile.component';
import { NotificationListComponent } from './components/users/notifications/notification-list/notification-list.component';
import { IncidentManagementComponent } from './components/users/agent/incident-management/incident-management.component';
import { IncidentCreateComponent } from './components/users/agent/incident-create/incident-create.component';

// MES composants (Rayen)
import { OrganizationListComponent } from './components/admin/organization/organization-list/organization-list.component';
import { OrganizationFormComponent } from './components/admin/organization/organization-form/organization-form.component';
import { OrganizationDetailComponent } from './components/admin/organization/organization-detail/organization-detail.component';
import { PartnerListComponent as AdminPartnerListComponent } from './components/admin/partner/partner-list/partner-list.component';
import { PartnerFormComponent } from './components/admin/partner/partner-form/partner-form.component';
import { ContractListComponent } from './components/admin/contract/contract-list/contract-list.component';
import { ContractRemindersComponent } from './components/admin/contract-reminders/contract-reminders.component';
import { ContractFormComponent } from './components/admin/contract/contract-form/contract-form.component';
import { OperatorListComponent } from './components/users/operator-partner/operator-list/operator-list.component';
import { OperatorDetailComponent } from './components/users/operator-partner/operator-detail/operator-detail.component';
import { PartnerListComponent as UserPartnerListComponent } from './components/users/operator-partner/partner-list/partner-list.component';
import { DashboardComponent } from './components/admin/dashboard/dashboard.component';

const routes: Routes = [
  // Authentication routes (without layout)
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'access-denied', component: AccessDeniedComponent },

  // Frontoffice
  {
    path: '',
    component: FrontofficeLayoutComponent,
    children: [
      { path: '', component: HomeComponent },
      { path: 'home', component: HomeComponent },
      { path: 'agent-dhasbord', component: AgentDhasbordComponent, canActivate: [AuthGuard] },
      { path: 'operator-dhasbord', component: OperatorDhasbordComponent, canActivate: [AuthGuard] },
      { path: 'passenger-dhasbord', component: PassengerDhasbordComponent, canActivate: [AuthGuard] },
      { path: 'transit', component: HomeComponent }, // Placeholder
      { path: 'carpool', component: HomeComponent }, // Placeholder
      { path: 'community', component: HomeComponent }, // Placeholder

      // Agent routes
      { path: 'agent-dhasbord', component: AgentDhasbordComponent, canActivate: [AuthGuard], data: { roles: ['AGENT'] } },
      { path: 'operator-dhasbord', component: OperatorDhasbordComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },

      // Passenger routes
      { path: 'passenger-dhasbord', component: PassengerDhasbordComponent, canActivate: [AuthGuard], data: { roles: ['PASSENGER'] } },

      // User Profile - Accessible by all authenticated users (AGENT, OPERATOR, PASSENGER)
      { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard], data: { roles: ['AGENT', 'OPERATOR', 'PASSENGER'] } },
      { path: 'notifications', component: NotificationListComponent, canActivate: [AuthGuard], data: { roles: ['AGENT', 'OPERATOR', 'PASSENGER'] } },
      { path: 'incidents', redirectTo: 'incidents/create', pathMatch: 'full' },
      { path: 'incidents/create', component: IncidentCreateComponent, canActivate: [AuthGuard], data: { roles: ['AGENT'] } },
      { path: 'incidents/list', component: IncidentManagementComponent, canActivate: [AuthGuard], data: { roles: ['AGENT'] } },

      // Document Management Routes - User side
      { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
      { path: 'documents', component: DocumentListComponent, canActivate: [AuthGuard] },
      { path: 'documents/upload', component: DocumentUploadComponent, canActivate: [AuthGuard] },
      { path: 'documents/:id', component: DocumentDetailComponent, canActivate: [AuthGuard] },
      { path: 'documents/:id/reupload', component: DocumentUploadComponent, canActivate: [AuthGuard] },
      { path: 'documents/:id/reupload', component: DocumentUploadComponent, canActivate: [AuthGuard] },
      // Passenger feature routes (from colleague)
      { path: 'passenger/plans', component: PassengerPlansComponent, canActivate: [AuthGuard], data: { roles: ['PASSENGER'] } },
      { path: 'passenger/subscriptions', component: PassengerSubscriptionsComponent, canActivate: [AuthGuard], data: { roles: ['PASSENGER'] } },
      { path: 'passenger/loyalty', component: PassengerLoyaltyComponent, canActivate: [AuthGuard], data: { roles: ['PASSENGER'] } },
      { path: 'payment/success', component: PaymentSuccessComponent },
      { path: 'payment/cancel', component: PaymentCancelComponent },

      // Operator feature routes (from colleague)
      { path: 'operator/pricing-plans', component: PricingPlanComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },
      { path: 'operator/subscriptions', component: OperatorSubscriptionsComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },
      { path: 'operator/reductions', component: ReductionComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },
      { path: 'operator/loyalty', component: OperatorLoyaltyComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },
      { path: 'operator/ml', component: MlDashboardComponent, canActivate: [AuthGuard], data: { roles: ['OPERATOR'] } },
      // MES routes user
      { path: 'operators', component: OperatorListComponent, canActivate: [AuthGuard] },
      { path: 'operators/:id', component: OperatorDetailComponent, canActivate: [AuthGuard] },
      { path: 'partners', component: UserPartnerListComponent, canActivate: [AuthGuard] },
      { path: 'partners/:id', component: UserPartnerListComponent, canActivate: [AuthGuard] },
      { path: 'partners', component: UserPartnerListComponent, canActivate: [AuthGuard] },

      // Transit Frontend Public Routes
      { path: 'public/lines', loadComponent: () => import('./public/lines-public/lines-public.component').then(m => m.LinesPublicComponent) },
      { path: 'public/schedules', loadComponent: () => import('./public/schedules-public/schedules-public.component').then(m => m.SchedulesPublicComponent) },
      { path: 'public/trips', loadComponent: () => import('./public/trips-public/trips-public.component').then(m => m.TripsPublicComponent) },
      { path: 'public/alerts', loadComponent: () => import('./public/alerts-public/alerts-public.component').then(m => m.AlertsPublicComponent) }
    ]
  },

  // Backoffice routes (with backoffice layout for admin)
  // Backoffice admin (layout coll�gue)
  // Ticket frontoffice (user)
  {
    path: 'ticket',
    component: FrontofficeLayoutComponent,
    canActivate: [AuthGuard],
    data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] },
    children: [
      { path: 'covoiturages', loadComponent: () => import('./user-ticket/covoiturage/covoiturage-list/covoiturage-list').then(m => m.CovoiturageListComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'covoiturages/new', loadComponent: () => import('./user-ticket/covoiturage/covoiturage-form/covoiturage-form').then(m => m.CovoiturageFormComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'my-covoiturages', loadComponent: () => import('./user-ticket/my-covoiturages/my-covoiturages').then(m => m.MyCovoituragesComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'reservations', loadComponent: () => import('./user-ticket/reservation/reservation-list/reservation-list').then(m => m.ReservationListComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'reservations/new', loadComponent: () => import('./user-ticket/reservation/reservation-form/reservation-form').then(m => m.ReservationFormComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'tickets', loadComponent: () => import('./user-ticket/ticket/ticket-list/ticket-list').then(m => m.TicketListComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } },
      { path: 'ai', loadComponent: () => import('./user-ticket/ai-dashboard/ai-dashboard').then(m => m.AIDashboardComponent), data: { roles: ['OPERATOR', 'PASSENGER', 'AGENT'] } }
    ]
  },

  // Admin backoffice (tout en un seul bloc)
  {
    path: 'admin',
    component: BackofficeLayoutComponent,
    canActivate: [AuthGuard],
    data: { roles: ['ADMIN'] },
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: AdminDhasbordComponent },
      { path: 'admin-dhasbord', component: AdminDhasbordComponent },
      { path: 'stats-dashboard', component: DashboardComponent },
      { path: 'users', component: AdminUserManagementComponent },
      { path: 'admin-dhasbord/users', component: AdminUserManagementComponent },
      { path: 'documents', component: AdminDocumentListComponent },
      { path: 'documents/types', component: DocumentTypeManagerComponent },
      { path: 'audit-log', component: AuditLogComponent },
      { path: 'expiry-alerts', component: DocumentExpiryAlertsComponent },
      // MES routes admin
      { path: 'organizations', component: OrganizationListComponent },
      { path: 'organizations/new', component: OrganizationFormComponent },
      { path: 'organizations/edit/:id', component: OrganizationFormComponent },
      { path: 'organizations/:id', component: OrganizationDetailComponent },
      { path: 'partners', component: AdminPartnerListComponent },
      { path: 'partners/new', component: PartnerFormComponent },
      { path: 'partners/edit/:id', component: PartnerFormComponent },
      { path: 'contracts', component: ContractListComponent },
      { path: 'contracts/new', component: ContractFormComponent },
      { path: 'contracts/edit/:id', component: ContractFormComponent },
      { path: 'contracts/reminders', component: ContractRemindersComponent },
      { path: 'ticket/covoiturages', loadComponent: () => import('./admin-ticket/covoiturage/covoiturage-list/covoiturage-list').then(m => m.CovoiturageListComponent) },
      { path: 'ticket/covoiturages/new', loadComponent: () => import('./admin-ticket/covoiturage/covoiturage-form/covoiturage-form').then(m => m.CovoiturageFormComponent) },
      { path: 'ticket/covoiturages/edit/:id', loadComponent: () => import('./admin-ticket/covoiturage/covoiturage-form/covoiturage-form').then(m => m.CovoiturageFormComponent) },
      { path: 'ticket/reservations', loadComponent: () => import('./admin-ticket/reservation/reservation-list/reservation-list').then(m => m.ReservationListComponent) },
      { path: 'ticket/tickets', loadComponent: () => import('./admin-ticket/ticket/ticket-list/ticket-list').then(m => m.TicketListComponent) },
      { path: 'ticket/tickets/new', loadComponent: () => import('./admin-ticket/ticket/ticket-form/ticket-form').then(m => m.TicketFormComponent) },
      { path: 'ticket/tickets/edit/:id', loadComponent: () => import('./admin-ticket/ticket/ticket-form/ticket-form').then(m => m.TicketFormComponent) },
      { path: 'ai-stats', loadComponent: () => import('./admin-ticket/ai-stats/ai-stats').then(m => m.AIStatsComponent), data: { roles: ['ADMIN'] } },

      // Transit Frontend Fleet Routes
      { path: 'vehicles', loadComponent: () => import('./vehicles/vehicle-list/vehicle-list.component').then(m => m.VehicleListComponent) },
      { path: 'vehicles/new', loadComponent: () => import('./vehicles/vehicle-form/vehicle-form.component').then(m => m.VehicleFormComponent) },
      { path: 'vehicles/edit/:id', loadComponent: () => import('./vehicles/vehicle-form/vehicle-form.component').then(m => m.VehicleFormComponent) },
      { path: 'maintenance', loadComponent: () => import('./maintenance/maintenance-list/maintenance-list.component').then(m => m.MaintenanceListComponent) },
      { path: 'maintenance/new', loadComponent: () => import('./maintenance/maintenance-form/maintenance-form.component').then(m => m.MaintenanceFormComponent) },
      { path: 'maintenance/edit/:id', loadComponent: () => import('./maintenance/maintenance-form/maintenance-form.component').then(m => m.MaintenanceFormComponent) },
      { path: 'lines', loadComponent: () => import('./lines/line-list/line-list.component').then(m => m.LineListComponent) },
      { path: 'lines/new', loadComponent: () => import('./lines/line-form/line-form.component').then(m => m.LineFormComponent) },
      { path: 'lines/edit/:id', loadComponent: () => import('./lines/line-form/line-form.component').then(m => m.LineFormComponent) },
      { path: 'stops', loadComponent: () => import('./stops/stop-list/stop-list.component').then(m => m.StopListComponent) },
      { path: 'stops/new', loadComponent: () => import('./stops/stop-form/stop-form.component').then(m => m.StopFormComponent) },
      { path: 'stops/edit/:id', loadComponent: () => import('./stops/stop-form/stop-form.component').then(m => m.StopFormComponent) },
      { path: 'schedules', loadComponent: () => import('./schedules/schedule-list/schedule-list.component').then(m => m.ScheduleListComponent) },
      { path: 'schedules/new', loadComponent: () => import('./schedules/schedule-form/schedule-form.component').then(m => m.ScheduleFormComponent) },
      { path: 'schedules/edit/:id', loadComponent: () => import('./schedules/schedule-form/schedule-form.component').then(m => m.ScheduleFormComponent) },
      { path: 'trips', loadComponent: () => import('./trips/trip-list/trip-list.component').then(m => m.TripListComponent) },
      { path: 'trips/new', loadComponent: () => import('./trips/trip-form/trip-form.component').then(m => m.TripFormComponent) },
      { path: 'trips/edit/:id', loadComponent: () => import('./trips/trip-form/trip-form.component').then(m => m.TripFormComponent) },
      { path: 'drivers', loadComponent: () => import('./metiers/drivers/driver-list/driver-list.component').then(m => m.DriverListComponent) },
      { path: 'drivers/new', loadComponent: () => import('./metiers/drivers/driver-form/driver-form.component').then(m => m.DriverFormComponent) },
      { path: 'drivers/edit/:id', loadComponent: () => import('./metiers/drivers/driver-form/driver-form.component').then(m => m.DriverFormComponent) },
      { path: 'fuel-logs', loadComponent: () => import('./metiers/fuel-logs/fuel-log-list/fuel-log-list.component').then(m => m.FuelLogListComponent) },
      { path: 'fuel-logs/new', loadComponent: () => import('./metiers/fuel-logs/fuel-log-form/fuel-log-form.component').then(m => m.FuelLogFormComponent) },
      { path: 'fuel-logs/edit/:id', loadComponent: () => import('./metiers/fuel-logs/fuel-log-form/fuel-log-form.component').then(m => m.FuelLogFormComponent) },
      { path: 'drivers/validation', loadComponent: () => import('./metiers/drivers/driver-validation/driver-validation.component').then(m => m.DriverValidationComponent) },
      { path: 'drivers/license/:id', loadComponent: () => import('./metiers/drivers/driver-license-upload/driver-license-upload.component').then(m => m.DriverLicenseUploadComponent) },
      { path: 'spare-parts', loadComponent: () => import('./metiers/spare-parts/spare-part-list/spare-part-list.component').then(m => m.SparePartListComponent) },
      { path: 'spare-parts/new', loadComponent: () => import('./metiers/spare-parts/spare-part-form/spare-part-form.component').then(m => m.SparePartFormComponent) },
      { path: 'spare-parts/edit/:id', loadComponent: () => import('./metiers/spare-parts/spare-part-form/spare-part-form.component').then(m => m.SparePartFormComponent) },
      { path: 'predictions', loadComponent: () => import('./metiers/prediction-page/prediction-page.component').then(m => m.PredictionPageComponent) },
      { path: 'part-usage', loadComponent: () => import('./metiers/part-usage/part-usage-page.component').then(m => m.PartUsagePageComponent) },
      { path: 'route-map', loadComponent: () => import('./metiers/route-map/route-map.component').then(m => m.RouteMapComponent) },
      { path: 'ai-stats', loadComponent: () => import('./admin-ticket/ai-stats/ai-stats').then(m => m.AIStatsComponent) }
    ]
  },

  // Old admin routes (redirect to new structure for backwards compatibility)
  { path: 'admin-dhasbord', redirectTo: '/admin/dashboard', pathMatch: 'full' },
  { path: 'admin-dhasbord/users', redirectTo: '/admin/users', pathMatch: 'full' },

  // Legacy redirect routes
  { path: 'dashboard', redirectTo: '/admin/dashboard', pathMatch: 'full' },

  // Redirect unknown routes to home
  { path: '**', redirectTo: '/' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }


