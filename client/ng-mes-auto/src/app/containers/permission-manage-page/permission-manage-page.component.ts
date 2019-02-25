import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, filter, finalize, map, switchMap, tap} from 'rxjs/operators';
import {PermissionUpdateDialogComponent} from '../../components/permission-update-dialog/permission-update-dialog.component';
import {Permission} from '../../models/permission';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../../store/actions/core';
import {DeleteSuccess, SaveSuccess} from '../../store/actions/permission-manage-page';
import {permissionManagePagePermissions} from '../../store/admin';

@Component({
  templateUrl: './permission-manage-page.component.html',
  styleUrls: ['./permission-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PermissionManagePageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-permission-manage-page') b2 = true;
  readonly displayedColumns = ['name', 'code', 'btns'];
  readonly permissions$ = this.store.select(permissionManagePagePermissions);

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  create() {
    this.update(new Permission());
  }

  update(permission: Permission) {
    PermissionUpdateDialogComponent.open(this.dialog, {permission})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({permission: it}));
        this.utilService.showSuccess();
      });
  }

  delete(permission: Permission) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deletePermission(permission.id)),
        map(() => new DeleteSuccess({id: permission.id})),
        tap(() => this.utilService.showSuccess()),
        catchError(error => of(new ShowError(error))),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(it => this.store.dispatch(it));
  }

}
