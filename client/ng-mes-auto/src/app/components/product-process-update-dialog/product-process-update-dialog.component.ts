import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {ROLES} from '../../models/permission';
import {ProductProcess} from '../../models/product-process';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {FormConfigModule} from '../form-config/form-config-input.component';
import {SilkExceptionModule} from '../silk-exception/silk-exception-input.component';
import {SilkNoteModule} from '../silk-note/silk-note-input.component';

@Component({
  templateUrl: './product-process-update-dialog.component.html',
  styleUrls: ['./product-process-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductProcessUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-product-processing-update-dialog') b2 = true;
  readonly dialogTitle = this.process.id ? 'Common.edit' : 'Common.create';
  readonly ROLES = ROLES;
  readonly form = this.fb.group({
    id: this.process.id,
    product: [this.process.product, Validators.required],
    name: [this.process.name, Validators.required],
    sortBy: [this.process.sortBy || 0, [Validators.required, Validators.min(0)]],
    exceptions: [this.process.exceptions, [Validators.required, Validators.min(1)]],
    notes: [this.process.notes],
    relateRoles: [this.process.relateRoles],
    formConfig: [this.process.formConfig]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<ProductProcessUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { process: ProductProcess }) {
  }

  get relateRolesCtrl() {
    return this.form.get('relateRoles');
  }

  get productCtrl() {
    return this.form.get('product');
  }

  get nameCtrl() {
    return this.form.get('name');
  }

  get sortByCtrl() {
    return this.form.get('sortBy');
  }

  get exceptionsCtrl() {
    return this.form.get('exceptions');
  }

  get formConfigCtrl() {
    return this.form.get('formConfig');
  }

  get notesCtrl() {
    return this.form.get('notes');
  }

  private get process() {
    return this.data.process;
  }

  static open(dialog: MatDialog, data: { process: ProductProcess }): MatDialogRef<ProductProcessUpdateDialogComponent, ProductProcess> {
    return dialog.open(ProductProcessUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveProductProcess(this.form.value).subscribe(
      it => {
        this.dialogRef.close(it);
      },
      err => {
        this.store.dispatch(new ShowError(err));
      }
    );
  }
}


@NgModule({
  imports: [
    SharedModule,
    FormConfigModule,
    SilkNoteModule,
    SilkExceptionModule
  ],
  declarations: [
    ProductProcessUpdateDialogComponent
  ],
  entryComponents: [
    ProductProcessUpdateDialogComponent
  ],
  exports: [
    ProductProcessUpdateDialogComponent
  ]
})
export class ProductProcessUpdateDialogComponentModule {
}
