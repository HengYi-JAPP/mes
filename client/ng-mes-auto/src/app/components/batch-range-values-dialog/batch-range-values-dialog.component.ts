import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {SharedModule} from '../../shared.module';

@Component({
  templateUrl: './batch-range-values-dialog.component.html',
  styleUrls: ['./batch-range-values-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BatchRangeValuesDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-batch-range-values-dialog') b2 = true;
  readonly values: string[];

  constructor(private dialogRef: MatDialogRef<BatchRangeValuesDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { values: string[] }) {
    this.values = data.values;
  }

  static open(dialog: MatDialog, data: { values: string[] }): MatDialogRef<BatchRangeValuesDialogComponent> {
    return dialog.open(BatchRangeValuesDialogComponent, {panelClass: 'my-dialog', data});
  }
}


@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    BatchRangeValuesDialogComponent
  ],
  entryComponents: [
    BatchRangeValuesDialogComponent
  ],
  exports: [
    BatchRangeValuesDialogComponent
  ]
})
export class BatchRangeValuesDialogComponentModule {
}
