import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ReportRootPageComponent} from './containers/report-root-page/report-root-page.component';

const routes: Routes = [
  {
    path: '',
    component: ReportRootPageComponent,
    children: [
      {path: '', redirectTo: 'workshops', pathMatch: 'full'}
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ReportRoutingModule {
}
