import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {AdminRoutingModule} from './admin-routing.module';
import {OperatorGroupUpdateDialogComponentModule} from './components/operator-group-update-dialog/operator-group-update-dialog.component';
import {OperatorImportDialogComponentModule} from './components/operator-import-dialog/operator-import-dialog.component';
import {OperatorUpdateDialogComponentModule} from './components/operator-update-dialog/operator-update-dialog.component';
import {PermissionUpdateDialogComponentModule} from './components/permission-update-dialog/permission-update-dialog.component';
import {AdminRootPageComponent} from './containers/admin-root-page/admin-root-page.component';
import {OperatorGroupManagePageComponent} from './containers/operator-group-manage-page/operator-group-manage-page.component';
import {OperatorManagePageComponent} from './containers/operator-manage-page/operator-manage-page.component';
import {PermissionManagePageComponent} from './containers/permission-manage-page/permission-manage-page.component';
import {SharedModule} from './shared.module';
import {featureName, reducers} from './store/admin';
import {OperatorGroupManagePageEffects} from './store/effects/operator-group-manage-page.effects';
import {OperatorManagePageEffects} from './store/effects/operator-manage-page.effects';
import {PermissionManagePageEffects} from './store/effects/permission-manage-page.effects';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      OperatorManagePageEffects,
      OperatorGroupManagePageEffects,
      PermissionManagePageEffects
    ]),
    PermissionUpdateDialogComponentModule,
    OperatorGroupUpdateDialogComponentModule,
    OperatorImportDialogComponentModule,
    OperatorUpdateDialogComponentModule,
    AdminRoutingModule
  ],
  declarations: [
    OperatorManagePageComponent,
    OperatorGroupManagePageComponent,
    PermissionManagePageComponent,
    AdminRootPageComponent
  ]
})
export class AdminModule {
}
