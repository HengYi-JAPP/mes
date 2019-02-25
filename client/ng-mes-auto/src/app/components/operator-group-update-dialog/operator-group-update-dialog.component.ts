import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {OperatorGroup} from '../../models/operator-group';
import {ROLES} from '../../models/permission';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {PermissionInputComponentModule} from '../permission-input/permission-input.component';

@Component({
  templateUrl: './operator-group-update-dialog.component.html',
  styleUrls: ['./operator-group-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperatorGroupUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-operator-group-update-dialog') b2 = true;
  readonly dialogTitle = this.operatorGroup.id ? 'Common.edit' : 'Common.create';
  readonly ROLES = ROLES;
  readonly form = this.fb.group({
    id: this.operatorGroup.id,
    name: [this.operatorGroup.name, Validators.required],
    roles: [this.operatorGroup.roles],
    permissions: [this.operatorGroup.permissions, [Validators.required, Validators.minLength(1)]]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private utilService: UtilService,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<OperatorGroupUpdateDialogComponent, OperatorGroup>,
              @Inject(MAT_DIALOG_DATA) private data: { operatorGroup: OperatorGroup }) {
  }

  get nameCtrl() {
    return this.form.get('name');
  }

  get permissionsCtrl() {
    return this.form.get('permissions');
  }

  get rolesCtrl() {
    return this.form.get('roles');
  }

  get operatorGroup() {
    return this.data.operatorGroup;
  }

  static open(dialog: MatDialog, data: { operatorGroup: OperatorGroup }): MatDialogRef<OperatorGroupUpdateDialogComponent, OperatorGroup> {
    return dialog.open(OperatorGroupUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveOperatorGroup(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}

@NgModule({
  imports: [
    SharedModule,
    PermissionInputComponentModule
  ],
  declarations: [
    OperatorGroupUpdateDialogComponent
  ],
  entryComponents: [
    OperatorGroupUpdateDialogComponent
  ],
  exports: [
    OperatorGroupUpdateDialogComponent
  ]
})
export class OperatorGroupUpdateDialogComponentModule {
}
