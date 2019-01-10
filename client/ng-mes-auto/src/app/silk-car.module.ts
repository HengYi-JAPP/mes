import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {ProductProcessSubmitDialogComponentModule} from './components/product-process-submit-dialog/product-process-submit-dialog.component';
import {SilkCarEventListComponentModule} from './components/silk-car-event-list/silk-car-event-list.component';
import {SilkCarInfoComponentModule} from './components/silk-car-info/silk-car-info.component';
import {SilkCarHistoryPageComponent} from './containers/silk-car-history-page/silk-car-history-page.component';
import {SilkCarRuntimePageComponent} from './containers/silk-car-runtime-page/silk-car-runtime-page.component';
import {SharedModule} from './shared.module';
import {SilkCarRoutingModule} from './silk-car-routing.module';
import {SilkHistoryPageEffects} from './store/effects/silk-history-page.effects';
import {SilkRuntimePageEffects} from './store/effects/silk-runtime-page.effects';
import {featureName, reducers} from './store/silk-car';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      SilkRuntimePageEffects,
      SilkHistoryPageEffects
    ]),
    SilkCarInfoComponentModule,
    SilkCarEventListComponentModule,
    ProductProcessSubmitDialogComponentModule,
    SilkCarRoutingModule
  ],
  declarations: [
    SilkCarRuntimePageComponent,
    SilkCarHistoryPageComponent
  ]
})
export class SilkCarModule {
}
