import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ConfigRootPageComponent} from './containers/config-root-page/config-root-page.component';
import {GradeManagePageComponent} from './containers/grade-manage-page/grade-manage-page.component';
import {LineMachineManagePageComponent} from './containers/line-machine-manage-page/line-machine-manage-page.component';
import {LineManagePageComponent} from './containers/line-manage-page/line-manage-page.component';
import {ProductConfigPageComponent} from './containers/product-config-page/product-config-page.component';
import {ProductManagePageComponent} from './containers/product-manage-page/product-manage-page.component';
import {SilkCarManagePageComponent} from './containers/silk-car-manage-page/silk-car-manage-page.component';
import {WorkshopManagePageComponent} from './containers/workshop-manage-page/workshop-manage-page.component';
import {AdminGuard} from './services/admin.guard';
import {CanDeactivateGuard} from './services/leave-current-can-deactivate.service';

const routes: Routes = [
  {
    path: '',
    component: ConfigRootPageComponent,
    children: [
      {path: '', redirectTo: 'workshops', pathMatch: 'full'},
      {
        path: 'workshops',
        component: WorkshopManagePageComponent
      },
      {
        path: 'products',
        component: ProductManagePageComponent
      },
      {
        path: 'products/:id/config',
        component: ProductConfigPageComponent,
        canActivate: [AdminGuard],
        canDeactivate: [CanDeactivateGuard]
      },
      {
        path: 'lines',
        component: LineManagePageComponent
      },
      {
        path: 'silkCars',
        component: SilkCarManagePageComponent
      },
      {
        path: 'lineMachines',
        component: LineMachineManagePageComponent
      },
      {
        path: 'grades',
        component: GradeManagePageComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConfigRoutingModule {
}
