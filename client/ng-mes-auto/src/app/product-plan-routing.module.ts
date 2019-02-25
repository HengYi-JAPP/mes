import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {BatchManagePageComponent} from './containers/batch-manage-page/batch-manage-page.component';
import {ProductPlanNotifyExeInfoPageComponent} from './containers/product-plan-notify-exe-info-page/product-plan-notify-exe-info-page.component';
import {ProductPlanNotifyManagePageComponent} from './containers/product-plan-notify-manage-page/product-plan-notify-manage-page.component';
import {ProductPlanRootPageComponent} from './containers/product-plan-root-page/product-plan-root-page.component';
import {WorkshopProductPlanReportPageComponent} from './containers/workshop-product-plan-report-page/workshop-product-plan-report-page.component';

const routes: Routes = [
  {
    path: '',
    component: ProductPlanRootPageComponent,
    children: [
      {path: '', redirectTo: 'workshopProductPlanReport', pathMatch: 'full'},
      {
        path: 'workshopProductPlanReport',
        component: WorkshopProductPlanReportPageComponent
      },
      {
        path: 'notifies',
        component: ProductPlanNotifyManagePageComponent
      },
      {
        path: 'notifies/:id/exeInfo',
        component: ProductPlanNotifyExeInfoPageComponent
      },
      {
        path: 'batches',
        component: BatchManagePageComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ProductPlanRoutingModule {
}
