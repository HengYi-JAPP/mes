import {ChangeDetectionStrategy, Component, HostBinding, Inject} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {FormConfig} from '../../models/form-config';
import {ApiService} from '../../services/api.service';

@Component({
  templateUrl: './form-config-preview-dialog.component.html',
  styleUrls: ['./form-config-preview-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormConfigPreviewDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-form-config-preview-dialog') b2 = true;
  readonly dialogTitle = 'Common.preview';

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<FormConfigPreviewDialogComponent, FormConfig>,
              @Inject(MAT_DIALOG_DATA) private data: { formConfig: FormConfig }) {
  }

  get formConfig(): FormConfig {
    return this.data.formConfig;
  }

  static open(dialog: MatDialog, data: { formConfig: FormConfig }): MatDialogRef<FormConfigPreviewDialogComponent, FormConfig> {
    return dialog.open(FormConfigPreviewDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

}
