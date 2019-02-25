import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {finalize} from 'rxjs/operators';
import {EventSourceTypes} from '../../models/event-source';
import {SilkCarRecord} from '../../models/silk-car-record';
import {SilkRuntime} from '../../models/silk-runtime';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {SetLoading, ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './dyeing-sample-silk-submit-dialog.component.html',
  styleUrls: ['./dyeing-sample-silk-submit-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DyeingSampleSilkSubmitDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-dyeing-sample-silk-submit-dialog') b2 = true;
  readonly dialogTitle = 'RoleType.SUBMIT_DYEING_SAMPLE';
  readonly form = this.fb.group({
    silkCarRecord: [this.silkCarRecord, Validators.required],
    silkRuntimes: [this.silkRuntimes, [Validators.required, Validators.minLength(1)]]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<DyeingSampleSilkSubmitDialogComponent, EventSourceTypes>,
              @Inject(MAT_DIALOG_DATA) private data: { silkCarRecord: SilkCarRecord, silkRuntimes: SilkRuntime[] }) {
  }

  get silkCarRecord() {
    return this.data.silkCarRecord;
  }

  get silkRuntimes() {
    return this.data.silkRuntimes;
  }

  get silkCar() {
    return this.silkCarRecord.silkCar;
  }

  static open(dialog: MatDialog, data: { silkCarRecord: SilkCarRecord, silkRuntimes?: SilkRuntime[] }): MatDialogRef<DyeingSampleSilkSubmitDialogComponent, EventSourceTypes> {
    return dialog.open(DyeingSampleSilkSubmitDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.store.dispatch(new SetLoading());
    this.apiService.dyeingSampleSubmitEvents(this.form.value)
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
    SharedModule
  ],
  declarations: [
    DyeingSampleSilkSubmitDialogComponent
  ],
  entryComponents: [
    DyeingSampleSilkSubmitDialogComponent
  ],
  exports: [
    DyeingSampleSilkSubmitDialogComponent
  ]
})
export class DyeingSampleSilkSubmitDialogComponentModule {
}
