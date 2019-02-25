import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {PrinterManagePageComponent} from './containers/printer-manage-page/printer-manage-page.component';
import {SilkBarcodeManagePageComponent} from './containers/silk-barcode-manage-page/silk-barcode-manage-page.component';
import {PrintRoutingModule} from './print-routing.module';
import {SharedModule} from './shared.module';
import {PrinterManagePageEffects} from './store/effects/printer-manage-page.effects';
import {SilkBarcodeManagePageEffects} from './store/effects/silk-barcode-manage-page.effects';
import {featureName, reducers} from './store/print';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      PrinterManagePageEffects,
      SilkBarcodeManagePageEffects
    ]),
    PrintRoutingModule
  ],
  declarations: [
    SilkBarcodeManagePageComponent,
    PrinterManagePageComponent
  ]
})
export class PrintModule {
}
