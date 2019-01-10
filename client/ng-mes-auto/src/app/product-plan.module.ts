import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {BatchUpdateDialogComponentModule} from './components/batch-update-dialog/batch-update-dialog.component';
import {ProductPlanNotifyUpdateDialogComponentModule} from './components/product-plan-notify-update-dialog/product-plan-notify-update-dialog.component';
import {BatchManagePageComponent} from './containers/batch-manage-page/batch-manage-page.component';
import {ProductPlanNotifyExeInfoPageComponent} from './containers/product-plan-notify-exe-info-page/product-plan-notify-exe-info-page.component';
import {ProductPlanNotifyManagePageComponent} from './containers/product-plan-notify-manage-page/product-plan-notify-manage-page.component';
import {ProductPlanRootPageComponent} from './containers/product-plan-root-page/product-plan-root-page.component';
import {WorkshopProductPlanReportPageComponent} from './containers/workshop-product-plan-report-page/workshop-product-plan-report-page.component';
import {ProductPlanRoutingModule} from './product-plan-routing.module';
import {SharedModule} from './shared.module';
import {BatchManagePageEffects} from './store/effects/batch-manage-page.effects';
import {ProductPlanNotifyExeInfoPageEffects} from './store/effects/product-plan-notify-exe-info-page.effects';
import {ProductPlanNotifyManagePageEffects} from './store/effects/product-plan-notify-manage-page.effects';
import {WorkshopProductPlanReportPageEffects} from './store/effects/workshop-product-plan-report-page.effects';
import {featureName, reducers} from './store/product-plan';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      WorkshopProductPlanReportPageEffects,
      ProductPlanNotifyManagePageEffects,
      ProductPlanNotifyExeInfoPageEffects,
      BatchManagePageEffects
    ]),
    ProductPlanNotifyUpdateDialogComponentModule,
    BatchUpdateDialogComponentModule,
    ProductPlanRoutingModule
  ],
  declarations: [
    WorkshopProductPlanReportPageComponent,
    ProductPlanNotifyManagePageComponent,
    ProductPlanNotifyExeInfoPageComponent,
    BatchManagePageComponent,
    ProductPlanRootPageComponent
  ]
})
export class ProductPlanModule {
}
