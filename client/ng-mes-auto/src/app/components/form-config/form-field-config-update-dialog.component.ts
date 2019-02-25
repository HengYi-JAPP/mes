import {ChangeDetectionStrategy, Component, HostBinding, Inject, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {merge, Subject} from 'rxjs';
import {startWith, takeUntil} from 'rxjs/operators';
import {FormFieldConfig} from '../../models/form-field-config';
import {ApiService} from '../../services/api.service';

@Component({
  templateUrl: './form-field-config-update-dialog.component.html',
  styleUrls: ['./form-field-config-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormFieldConfigUpdateDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-form-field-config-update-dialog') b2 = true;
  readonly ALL_VALUE_TYPE = FormFieldConfig.ALL_VALUE_TYPE;
  readonly ALLL_INPUT_TYPE = FormFieldConfig.ALLL_INPUT_TYPE;
  readonly form = this.fb.group({
    id: this.formFieldConfig.id,
    required: [this.formFieldConfig.required, Validators.required],
    multi: [this.formFieldConfig.multi, Validators.required],
    name: [this.formFieldConfig.name, Validators.required],
    valueType: [this.formFieldConfig.valueType || 'STRING', Validators.required],
    inputType: [this.formFieldConfig.inputType || 'DEFAULT', Validators.required],
    selectOptions: [this.formFieldConfig.selectOptions, [Validators.required, Validators.minLength(1)]]
  });
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<FormFieldConfigUpdateDialogComponent, FormFieldConfig>,
              @Inject(MAT_DIALOG_DATA) private  data: { formFieldConfig: FormFieldConfig, dialogTitle: string }) {
  }

  get dialogTitle() {
    return this.data.dialogTitle;
  }

  get valueTypeCtrl() {
    return this.form.get('valueType');
  }

  get inputTypeCtrl() {
    return this.form.get('inputType');
  }

  get selectOptionsCtrl() {
    return this.form.get('selectOptions');
  }

  private get formFieldConfig() {
    return this.data.formFieldConfig;
  }

  static create(dialog: MatDialog, data: { formFieldConfig: FormFieldConfig }): MatDialogRef<FormFieldConfigUpdateDialogComponent, FormFieldConfig> {
    Object.assign(data, {dialogTitle: 'FormConfig.FieldConfig.new'});
    return dialog.open(FormFieldConfigUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  static update(dialog: MatDialog, data: { formFieldConfig: FormFieldConfig }): MatDialogRef<FormFieldConfigUpdateDialogComponent, FormFieldConfig> {
    Object.assign(data, {dialogTitle: 'FormConfig.FieldConfig.edit'});
    return dialog.open(FormFieldConfigUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.dialogRef.close(this.form.value);
  }

  ngOnInit(): void {
    merge(this.valueTypeCtrl.valueChanges, this.inputTypeCtrl.valueChanges)
      .pipe(
        takeUntil(this._destroy$),
        startWith('')
      )
      .subscribe(() => {
        const b = FormFieldConfig.hasSelectOptions(this.valueTypeCtrl.value, this.inputTypeCtrl.value);
        if (b) {
          this.selectOptionsCtrl.enable();
        } else {
          this.selectOptionsCtrl.disable();
        }
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

}
