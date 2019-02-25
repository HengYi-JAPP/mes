import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {SilkCarHistoryPageComponent} from './containers/silk-car-history-page/silk-car-history-page.component';
import {SilkCarRuntimePageComponent} from './containers/silk-car-runtime-page/silk-car-runtime-page.component';

const routes: Routes = [
  {path: '', redirectTo: 'runtime', pathMatch: 'full'},
  {
    path: 'runtime',
    component: SilkCarRuntimePageComponent
  },
  {
    path: 'history',
    component: SilkCarHistoryPageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SilkCarRoutingModule {
}
