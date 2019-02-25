import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AdminRootPageComponent} from './containers/admin-root-page/admin-root-page.component';
import {OperatorGroupManagePageComponent} from './containers/operator-group-manage-page/operator-group-manage-page.component';
import {OperatorManagePageComponent} from './containers/operator-manage-page/operator-manage-page.component';
import {PermissionManagePageComponent} from './containers/permission-manage-page/permission-manage-page.component';

const routes: Routes = [
  {
    path: '',
    component: AdminRootPageComponent,
    children: [
      {path: '', redirectTo: 'operators', pathMatch: 'full'},
      {
        path: 'operators',
        component: OperatorManagePageComponent
      },
      {
        path: 'operatorGroups',
        component: OperatorGroupManagePageComponent
      },
      {
        path: 'permissions',
        component: PermissionManagePageComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AdminRoutingModule {
}
