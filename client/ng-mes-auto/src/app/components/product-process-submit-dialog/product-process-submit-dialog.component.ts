import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {finalize} from 'rxjs/operators';
import {EventSourceTypes} from '../../models/event-source';
import {ProductProcess} from '../../models/product-process';
import {SilkCarRecord} from '../../models/silk-car-record';
import {SilkRuntime} from '../../models/silk-runtime';
import {ApiService} from '../../services/api.service';
import {compareWithId} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SetLoading, ShowError} from '../../store/actions/core';
import {FormConfigValueModule} from '../form-config-value/form-config-value-input.component';
import {FormConfigModule} from '../form-config/form-config-input.component';
import {SilkExceptionModule} from '../silk-exception/silk-exception-input.component';
import {SilkNoteModule} from '../silk-note/silk-note-input.component';

@Component({
  templateUrl: './product-process-submit-dialog.component.html',
  styleUrls: ['./product-process-submit-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductProcessSubmitDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-product-processing-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly dialogTitle = this.productProcess.name;
  readonly form = this.fb.group({
    silkCarRecord: [this.silkCarRecord, Validators.required],
    silkRuntimes: [this.silkRuntimes],
    productProcess: [this.productProcess, Validators.required],
    silkExceptions: null,
    silkNotes: null,
    formConfig: [this.formConfig],
    formConfigValueData: null
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<ProductProcessSubmitDialogComponent, EventSourceTypes>,
              @Inject(MAT_DIALOG_DATA) private data: { silkCarRecord: SilkCarRecord, silkRuntimes: SilkRuntime[], productProcess: ProductProcess }) {
  }

  get silkCarRecord() {
    return this.data.silkCarRecord;
  }

  get productProcess() {
    return this.data.productProcess;
  }

  get silkRuntimes() {
    return this.data.silkRuntimes;
  }

  get silkCar() {
    return this.silkCarRecord.silkCar;
  }

  get formConfig() {
    return this.productProcess.formConfig;
  }

  static open(dialog: MatDialog, data: { silkCarRecord: SilkCarRecord, silkRuntimes?: SilkRuntime[], productProcess: ProductProcess }): MatDialogRef<ProductProcessSubmitDialogComponent, EventSourceTypes> {
    return dialog.open(ProductProcessSubmitDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.store.dispatch(new SetLoading());
    this.apiService.productProcessSubmitEvents(this.form.value)
      .pipe(
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(
        () => this.dialogRef.close(),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}


@NgModule({
  imports: [
    SharedModule,
    FormConfigModule,
    FormConfigValueModule,
    SilkNoteModule,
    SilkExceptionModule
  ],
  declarations: [
    ProductProcessSubmitDialogComponent
  ],
  entryComponents: [
    ProductProcessSubmitDialogComponent
  ],
  exports: [
    ProductProcessSubmitDialogComponent
  ]
})
export class ProductProcessSubmitDialogComponentModule {
}
