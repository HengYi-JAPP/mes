import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {GradeUpdateDialogComponentModule} from './components/grade-update-dialog/grade-update-dialog.component';
import {LineMachineUpdateDialogComponentModule} from './components/line-machine-update-dialog/line-machine-update-dialog.component';
import {LineUpdateDialogComponentModule} from './components/line-update-dialog/line-update-dialog.component';
import {ProductProcessListComponentModule} from './components/product-process-list/product-process-list.component';
import {ProductProcessUpdateDialogComponentModule} from './components/product-process-update-dialog/product-process-update-dialog.component';
import {ProductUpdateDialogComponentModule} from './components/product-update-dialog/product-update-dialog.component';
import {SilkCarUpdateDialogComponentModule} from './components/silk-car-update-dialog/silk-car-update-dialog.component';
import {SilkExceptionModule} from './components/silk-exception/silk-exception-input.component';
import {SilkNoteModule} from './components/silk-note/silk-note-input.component';
import {WorkshopUpdateDialogComponentModule} from './components/workshop-update-dialog/workshop-update-dialog.component';
import {ConfigRoutingModule} from './config-routing.module';
import {ConfigRootPageComponent} from './containers/config-root-page/config-root-page.component';
import {GradeManagePageComponent} from './containers/grade-manage-page/grade-manage-page.component';
import {LineMachineManagePageComponent} from './containers/line-machine-manage-page/line-machine-manage-page.component';
import {LineManagePageComponent} from './containers/line-manage-page/line-manage-page.component';
import {ProductConfigPageComponent} from './containers/product-config-page/product-config-page.component';
import {ProductManagePageComponent} from './containers/product-manage-page/product-manage-page.component';
import {SilkCarManagePageComponent} from './containers/silk-car-manage-page/silk-car-manage-page.component';
import {WorkshopManagePageComponent} from './containers/workshop-manage-page/workshop-manage-page.component';
import {SharedModule} from './shared.module';
import {featureName, reducers} from './store/config';
import {GradeManagePageEffects} from './store/effects/grade-manage-page.effects';
import {LineMachineManagePageEffects} from './store/effects/line-machine-manage-page.effects';
import {LineManagePageEffects} from './store/effects/line-manage-page.effects';
import {ProductConfigPageEffects} from './store/effects/product-config-page.effects';
import {ProductManagePageEffects} from './store/effects/product-manage-page.effects';
import {SilkCarManagePageEffects} from './store/effects/silk-car-manage-page.effects';
import {WorkshopManagePageEffects} from './store/effects/workshop-manage-page.effects';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      WorkshopManagePageEffects,
      ProductManagePageEffects,
      ProductConfigPageEffects,
      LineManagePageEffects,
      SilkCarManagePageEffects,
      LineMachineManagePageEffects,
      GradeManagePageEffects
    ]),
    WorkshopUpdateDialogComponentModule,
    ProductProcessListComponentModule,
    ProductUpdateDialogComponentModule,
    ProductProcessUpdateDialogComponentModule,
    SilkExceptionModule,
    SilkNoteModule,
    LineUpdateDialogComponentModule,
    SilkCarUpdateDialogComponentModule,
    LineMachineUpdateDialogComponentModule,
    GradeUpdateDialogComponentModule,
    ConfigRoutingModule
  ],
  declarations: [
    WorkshopManagePageComponent,
    ProductManagePageComponent,
    ProductConfigPageComponent,
    LineManagePageComponent,
    SilkCarManagePageComponent,
    LineMachineManagePageComponent,
    GradeManagePageComponent,
    ConfigRootPageComponent
  ]
})
export class ConfigModule {
}
