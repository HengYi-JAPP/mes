import {ChangeDetectionStrategy, Component, HostBinding, Inject} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {SilkException} from '../../models/silk-exception';
import {ApiService} from '../../services/api.service';
import {compareWithId} from '../../services/util.service';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './silk-exception-update-dialog.component.html',
  styleUrls: ['./silk-exception-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkExceptionUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-silk-exception-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly dialogTitle = this.silkException.id ? 'Common.edit' : 'Common.create';
  readonly form = this.fb.group({
    id: this.silkException.id,
    name: [this.silkException.name, Validators.required]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<SilkExceptionUpdateDialogComponent, SilkException>,
              @Inject(MAT_DIALOG_DATA) private data: { silkException: SilkException }) {
  }

  get silkException() {
    return this.data.silkException;
  }

  static open(dialog: MatDialog, data: { silkException: SilkException }): MatDialogRef<SilkExceptionUpdateDialogComponent, SilkException> {
    return dialog.open(SilkExceptionUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveSilkException(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}
