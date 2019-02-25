import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Permission} from '../../models/permission';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './permission-update-dialog.component.html',
  styleUrls: ['./permission-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PermissionUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-permission-update-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly form: FormGroup;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<PermissionUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { permission: Permission }) {
    const {permission} = data;
    this.dialogTitle = permission.id ? 'Common.edit' : 'Common.create';
    this.form = fb.group({
      id: permission.id,
      name: [permission.name, Validators.required],
      code: [permission.code, Validators.required]
    });
  }

  get nameCtrl() {
    return this.form.get('name');
  }

  get code() {
    return this.form.get('code');
  }

  static open(dialog: MatDialog, data: { permission: Permission }): MatDialogRef<PermissionUpdateDialogComponent, Permission> {
    return dialog.open(PermissionUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.savePermission(this.form.value).subscribe(
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
    SharedModule
  ],
  declarations: [
    PermissionUpdateDialogComponent
  ],
  entryComponents: [
    PermissionUpdateDialogComponent
  ],
  exports: [
    PermissionUpdateDialogComponent
  ]
})
export class PermissionUpdateDialogComponentModule {
}
