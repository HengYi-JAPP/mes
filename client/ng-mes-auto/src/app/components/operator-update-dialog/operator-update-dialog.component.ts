import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Operator} from '../../models/operator';
import {ROLES} from '../../models/permission';
import {ApiService} from '../../services/api.service';
import {compareWithId} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {PermissionInputComponentModule} from '../permission-input/permission-input.component';

@Component({
  templateUrl: './operator-update-dialog.component.html',
  styleUrls: ['./operator-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperatorUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-operator-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly ROLES = ROLES;
  readonly dialogTitle = this.operator.name;
  readonly operatorGroups$ = this.apiService.listOperatorGroup();
  readonly form = this.fb.group({
    id: this.operator.id,
    name: [this.operator.name, Validators.required],
    admin: [this.operator.admin],
    roles: [this.operator.roles],
    groups: [this.operator.groups],
    permissions: [this.operator.permissions]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<OperatorUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA) private data: { operator: Operator }) {
  }

  get operator() {
    return this.data.operator;
  }

  get rolesCtrl() {
    return this.form.get('roles');
  }

  get permissionsCtrl() {
    return this.form.get('permissions');
  }

  get groupsCtrl() {
    return this.form.get('groups');
  }

  get nameCtrl() {
    return this.form.get('name');
  }

  get adminCtrl() {
    return this.form.get('admin');
  }

  static open(dialog: MatDialog, data: { operator: Operator }): MatDialogRef<OperatorUpdateDialogComponent, Operator> {
    return dialog.open(OperatorUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveOperator(this.form.value)
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
    OperatorUpdateDialogComponent
  ],
  entryComponents: [
    OperatorUpdateDialogComponent
  ],
  exports: [
    OperatorUpdateDialogComponent
  ]
})
export class OperatorUpdateDialogComponentModule {
}
