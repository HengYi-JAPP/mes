import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {Observable} from 'rxjs';
import {filter} from 'rxjs/operators';
import {ProductUpdateDialogComponent} from '../../components/product-update-dialog/product-update-dialog.component';
import {Product} from '../../models/product';
import {UtilService} from '../../services/util.service';
import {productManagePageProducts} from '../../store/config';
import {coreAuthAdmin} from '../../store/core';

@Component({
  templateUrl: './product-manage-page.component.html',
  styleUrls: ['./product-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductManagePageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-product-manage-page') b2 = true;
  readonly displayedColumns = ['name', 'btns'];
  readonly coreAuthAdmin$: Observable<boolean>;
  readonly products$: Observable<Product[]>;

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private translate: TranslateService,
              private utilService: UtilService) {
    this.coreAuthAdmin$ = this.store.select(coreAuthAdmin);
    this.products$ = this.store.select(productManagePageProducts);
  }

  create() {
    this.update(Product.assign());
  }

  update(product: Product) {
    ProductUpdateDialogComponent.open(this.dialog, {product})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.utilService.showSuccess();
        const queryParams = {productId: it.id};
        this.router.navigate(['config', 'products'], {queryParams});
      });
  }

  delete(product: Product) {
  }

}
