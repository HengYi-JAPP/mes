import {NgModule} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreModule} from '@ngrx/store';
import {StatisticsReportExportDialogComponentModule} from './components/statistics-report-export-dialog/statistics-report-export-dialog.component';
import {MeasureReportPageComponent} from './containers/measure-report-page/measure-report-page.component';
import {ReportRootPageComponent} from './containers/report-root-page/report-root-page.component';
import {StatisticsReportPageComponent} from './containers/statistics-report-page/statistics-report-page.component';
import {ReportRoutingModule} from './report-routing.module';
import {SharedModule} from './shared.module';
import {MeasureReportPageEffects} from './store/effects/measure-report-page.effects';
import {StatisticsReportPageEffects} from './store/effects/statistics-report-page.effects';
import {featureName, reducers} from './store/report';

@NgModule({
  imports: [
    SharedModule,
    StoreModule.forFeature(featureName, reducers),
    EffectsModule.forFeature([
      MeasureReportPageEffects,
      StatisticsReportPageEffects,
    ]),
    ReportRoutingModule,
    StatisticsReportExportDialogComponentModule,
  ],
  declarations: [
    ReportRootPageComponent,
    MeasureReportPageComponent,
    StatisticsReportPageComponent,
  ]
})
export class ReportModule {
}
