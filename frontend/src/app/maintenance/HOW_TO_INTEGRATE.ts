// ══════════════════════════════════════════════════════════════════
// 1. app.routes.ts — add these imports
// ══════════════════════════════════════════════════════════════════
//import { SparePartListComponent } from './spare-parts/spare-part-list/spare-part-list.component';
//import { SparePartFormComponent } from './spare-parts/spare-part-form/spare-part-form.component';

// add inside routes array:/
//{ path: 'spare-parts',          component: SparePartListComponent },
//{ path: 'spare-parts/new',      component: SparePartFormComponent },
//{ path: 'spare-parts/edit/:id', component: SparePartFormComponent },


// ══════════════════════════════════════════════════════════════════
// 2. sidebar.component.html — add under FLEET section
// ══════════════════════════════════════════════════════════════════
/*
<li>
  <a routerLink="/spare-parts" routerLinkActive="active" class="nav-link text-white">
    <i class="bi bi-box-seam me-2"></i> Spare Parts
  </a>
</li>
*/


// ══════════════════════════════════════════════════════════════════
// 3. EMBEDDING the part-usage panel into your maintenance form
//    Add this at the bottom of maintenance-form.component.html
//    ONLY when editing (isEdit = true)
// ══════════════════════════════════════════════════════════════════
/*
<app-part-usage-panel
  *ngIf="isEdit"
  [maintenanceOrderId]="id"
  [maintenanceType]="maintenance.type">
</app-part-usage-panel>
*/

// And add to maintenance-form.component.ts imports array:
// import { PartUsagePanelComponent } from '../../part-usage/part-usage-panel.component';
// Add to @Component imports: [..., PartUsagePanelComponent]
