import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Observable} from 'rxjs';
import {Batch} from '../../models/batch';
import {Product} from '../../models/product';
import {Workshop} from '../../models/workshop';
import {ApiService} from '../../services/api.service';
import {compareWithId} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './batch-update-dialog.component.html',
  styleUrls: ['./batch-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BatchUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-batch-update-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly form: FormGroup;
  readonly compareWithId = compareWithId;
  readonly workshops$: Observable<Workshop[]>;
  readonly product$: Observable<Product[]>;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<BatchUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { batch: Batch }) {
    this.workshops$ = this.apiService.listWorkshop();
    this.product$ = this.apiService.listProduct();
    const {batch} = data;
    this.dialogTitle = batch.id ? 'Common.edit' : 'Common.create';
    this.form = fb.group({
      id: batch.id,
      workshop: [batch.workshop, Validators.required],
      product: [batch.product, Validators.required],
      batchNo: [batch.batchNo, Validators.required],
      centralValue: [batch.centralValue, [Validators.required, Validators.min(0)]],
      silkWeight: [batch.silkWeight, [Validators.required, Validators.min(0)]],
      holeNum: [batch.holeNum, [Validators.required, Validators.min(0)]],
      spec: [batch.spec, Validators.required],
      tubeColor: [batch.tubeColor, Validators.required],
      note: batch.note
    });
  }

  get workshop() {
    return this.form.get('workshop');
  }

  get product() {
    return this.form.get('product');
  }

  get batchNo() {
    return this.form.get('batchNo');
  }

  get centralValue() {
    return this.form.get('centralValue');
  }

  get holeNum() {
    return this.form.get('holeNum');
  }

  get spec() {
    return this.form.get('spec');
  }

  get tubeColor() {
    return this.form.get('tubeColor');
  }

  get silkWeight() {
    return this.form.get('silkWeight');
  }

  get note() {
    return this.form.get('note');
  }

  static open(dialog: MatDialog, data: { batch: Batch }): MatDialogRef<BatchUpdateDialogComponent, Batch> {
    return dialog.open(BatchUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveBatch(this.form.value)
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
    BatchUpdateDialogComponent
  ],
  entryComponents: [
    BatchUpdateDialogComponent
  ],
  exports: [
    BatchUpdateDialogComponent
  ]
})
export class BatchUpdateDialogComponentModule {
}
