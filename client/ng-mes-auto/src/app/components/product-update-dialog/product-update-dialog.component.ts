import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Product} from '../../models/product';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './product-update-dialog.component.html',
  styleUrls: ['./product-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-product-update-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly form: FormGroup;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<ProductUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { product: Product }) {
    const {product} = data;
    this.dialogTitle = product.id ? 'Common.edit' : 'Common.create';
    this.form = fb.group({
      id: product.id,
      name: [product.name, Validators.required]
    });
  }

  get name() {
    return this.form.get('name');
  }

  static open(dialog: MatDialog, data: { product: Product }): MatDialogRef<ProductUpdateDialogComponent, Product> {
    return dialog.open(ProductUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveProduct(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}


@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    ProductUpdateDialogComponent
  ],
  entryComponents: [
    ProductUpdateDialogComponent
  ],
  exports: [
    ProductUpdateDialogComponent
  ]
})
export class ProductUpdateDialogComponentModule {
}
