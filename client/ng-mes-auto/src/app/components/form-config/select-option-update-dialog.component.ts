import {ChangeDetectionStrategy, Component, HostBinding, Inject} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';

@Component({
  templateUrl: './select-option-update-dialog.component.html',
  styleUrls: ['./select-option-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SelectOptionUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-form-field-config-selection-value-update-dialog') b2 = true;
  readonly form = this.fb.group({
    selectOption: [this.selectOption, Validators.required]
  });
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialogRef: MatDialogRef<SelectOptionUpdateDialogComponent, string>,
              @Inject(MAT_DIALOG_DATA) private  data: { selectOption: string, dialogTitle: string }) {
  }

  get dialogTitle() {
    return this.data.dialogTitle;
  }

  private get selectOption() {
    return this.data.selectOption;
  }

  static create(dialog: MatDialog): MatDialogRef<SelectOptionUpdateDialogComponent, string> {
    const data = {dialogTitle: 'FormConfig.FieldConfig.SelectOption.new'};
    return dialog.open(SelectOptionUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  static update(dialog: MatDialog, data: { selectOption: string }): MatDialogRef<SelectOptionUpdateDialogComponent, string> {
    Object.assign(data, {dialogTitle: 'FormConfig.FieldConfig.SelectOption.edit'});
    return dialog.open(SelectOptionUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.dialogRef.close(this.form.value.selectOption);
  }

}
