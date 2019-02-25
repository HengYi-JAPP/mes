import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSelectionList} from '@angular/material';
import {BehaviorSubject, Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {Permission} from '../../models/permission';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';

class State {
  allPermissions: Permission[] = [];
  originPermissions: Permission[] = [];
  q: string;
}

const getAllPermissions = (state: State) => state.allPermissions;
const getOriginPermissions = (state: State) => state.originPermissions;
const getQ = (state: State) => state.q;

@Component({
  templateUrl: './permission-pick-dialog.component.html',
  styleUrls: ['./permission-pick-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PermissionPickDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-permission-pick-dialog') b2 = true;
  @ViewChild(MatSelectionList) selectionList: MatSelectionList;
  readonly dialogTitle: string;
  readonly permissions$: Observable<Permission[]>;
  private readonly state$ = new BehaviorSubject(new State());
  readonly allPermissions$ = this.state$.pipe(map(getAllPermissions));
  readonly originPermissions$ = this.state$.pipe(map(getOriginPermissions));

  constructor(private dialog: MatDialog,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<PermissionPickDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { permissions: Permission[] }) {
    const {permissions} = data;
    this.apiService.listPermission()
      .subscribe(allPermissions => {
        const next = {...this.state$.value, allPermissions, originPermissions: permissions || []};
        this.state$.next(next);
      });
  }

  static open(dialog: MatDialog, data: { permissions: Permission[] }): MatDialogRef<PermissionPickDialogComponent, Permission[]> {
    return dialog.open(PermissionPickDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    const permissions = this.selectionList.selectedOptions.selected.map(it => it.value);
    this.dialogRef.close(permissions);
  }
}


@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    PermissionPickDialogComponent
  ],
  entryComponents: [
    PermissionPickDialogComponent
  ],
  exports: [
    PermissionPickDialogComponent
  ]
})
export class PermissionPickDialogComponentModule {
}
