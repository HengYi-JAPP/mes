import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MeasureReportPageComponent} from './containers/measure-report-page/measure-report-page.component';
import {ReportRootPageComponent} from './containers/report-root-page/report-root-page.component';
import {StatisticsReportPageComponent} from './containers/statistics-report-page/statistics-report-page.component';

const routes: Routes = [
  {
    path: '',
    component: ReportRootPageComponent,
    children: [
      {path: '', redirectTo: 'report', pathMatch: 'full'},
      {
        path: 'measureReport',
        component: MeasureReportPageComponent
      },
      {
        path: 'statisticsReport',
        component: StatisticsReportPageComponent
      },
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule {
}
