import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {ReportRootPageComponent} from './containers/report-root-page/report-root-page.component';
import {ReportRoutingModule} from './report-routing.module';
import {SharedModule} from './shared.module';
import {featureName, reducers} from './store/report';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([]),
    ReportRoutingModule
  ],
  declarations: [
    ReportRootPageComponent
  ]
})
export class SilkModule {
}
