import {ChangeDetectionStrategy, Component, HostBinding, ViewChild} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {from, Observable} from 'rxjs';
import {defaultIfEmpty, filter, map, switchMap, switchMapTo, take} from 'rxjs/operators';
import {ProductProcessListComponent} from '../../components/product-process-list/product-process-list.component';
import {ProductUpdateDialogComponent} from '../../components/product-update-dialog/product-update-dialog.component';
import {Product} from '../../models/product';
import {CanComponentDeactivate} from '../../services/leave-current-can-deactivate.service';
import {UtilService} from '../../services/util.service';
import {productConfigPageProduct, productConfigPageProducts} from '../../store/config';

@Component({
  templateUrl: './product-config-page.component.html',
  styleUrls: ['./product-config-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductConfigPageComponent implements CanComponentDeactivate {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-product-config-page') b2 = true;
  @ViewChild(ProductProcessListComponent) pplComp: ProductProcessListComponent;
  readonly products$: Observable<Product[]>;
  readonly product$: Observable<Product>;

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private translate: TranslateService,
              private utilService: UtilService) {
    this.products$ = this.store.select(productConfigPageProducts);
    this.product$ = this.store.select(productConfigPageProduct);
  }

  create() {
    ProductUpdateDialogComponent.open(this.dialog, {product: Product.assign()})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(product => {
        this.router.navigate(['config/products', product.id, 'config']);
        this.utilService.showSuccess();
      });
  }

  canDeactivate() {
    return this.pplComp.processForms$
      .pipe(
        take(1),
        switchMap(it => from(it)),
        filter(it => it.dirty),
        take(1),
        switchMapTo(this.translate.get('Common.canComponentDeactivate')),
        map(it => confirm(it)),
        defaultIfEmpty(true)
      );
  }

}
