import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ShellPageComponent} from './containers/shell-page/shell-page.component';
import {AdminGuard} from './services/admin.guard';

const routes: Routes = [
  {
    path: '',
    component: ShellPageComponent,
    children: [
      {path: '', redirectTo: '/config', pathMatch: 'full'},
      {path: 'print', loadChildren: './print.module#PrintModule'},
      {path: 'silkCar', loadChildren: './silk-car.module#SilkCarModule'},
      {path: 'config', loadChildren: './config.module#ConfigModule'},
      {path: 'productPlan', loadChildren: './product-plan.module#ProductPlanModule'},
      {path: 'report', loadChildren: './report.module#ReportModule'},
      {path: 'admin', loadChildren: './admin.module#AdminModule', canActivate: [AdminGuard]}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class CoreRoutingModule {
}
