import {ChangeDetectionStrategy, Component, HostBinding, Inject} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {FormConfig} from '../../models/form-config';
import {ApiService} from '../../services/api.service';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './form-config-update-dialog.component.html',
  styleUrls: ['./form-config-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormConfigUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-form-config-update-dialog') b2 = true;
  readonly form = this.fb.group({
    id: this.formConfig.id,
    name: [this.formConfig.name, Validators.required],
    formFieldConfigs: [this.formConfig.formFieldConfigs, [Validators.required, Validators.minLength(1)]]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<FormConfigUpdateDialogComponent, FormConfig>,
              @Inject(MAT_DIALOG_DATA) private  data: { formConfig: FormConfig }) {
  }

  get dialogTitle() {
    return 'FormConfig.' + (this.formConfig.id ? 'edit' : 'new');
  }

  private get formConfig() {
    return this.data.formConfig;
  }

  static open(dialog: MatDialog, data: { formConfig: FormConfig }): MatDialogRef<FormConfigUpdateDialogComponent, FormConfig> {
    return dialog.open(FormConfigUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveFormConfig(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}
