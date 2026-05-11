// ══════════════════════════════════════════════════════════════════
// 1. app.routes.ts — add import + route
// ══════════════════════════════════════════════════════════════════
//import { PredictionPageComponent } from './predictions/prediction-page/prediction-page.component';

// inside routes array:
//{ path: 'predictions', component: PredictionPageComponent },


// ══════════════════════════════════════════════════════════════════
// 2. sidebar.component.html — add link
// ══════════════════════════════════════════════════════════════════
/*
<li>
  <a routerLink="/admin/predictions" routerLinkActive="active" class="nav-link text-white">
    <i class="bi bi-cpu me-2"></i> Parts Prediction
  </a>
</li>
*/


// ══════════════════════════════════════════════════════════════════
// 3. Embed widget in spare-part-list.component.html
//    Add at the very bottom of the template:
// ══════════════════════════════════════════════════════════════════
/*
<app-prediction-widget></app-prediction-widget>
*/

// And in spare-part-list.component.ts add to imports array:
// import { PredictionWidgetComponent } from '../../prediction-widget/prediction-widget.component';
// imports: [..., PredictionWidgetComponent]
