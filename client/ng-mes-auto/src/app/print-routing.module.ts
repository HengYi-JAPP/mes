import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PrinterManagePageComponent} from './containers/printer-manage-page/printer-manage-page.component';
import {SilkBarcodeManagePageComponent} from './containers/silk-barcode-manage-page/silk-barcode-manage-page.component';

const routes: Routes = [
  {path: '', redirectTo: 'silkBarcodes', pathMatch: 'full'},
  {
    path: 'silkBarcodes',
    component: SilkBarcodeManagePageComponent
  },
  {
    path: 'printers',
    component: PrinterManagePageComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PrintRoutingModule {
}
