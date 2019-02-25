import {ChangeDetectionStrategy, Component, forwardRef, NgModule} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog, MatTableDataSource} from '@angular/material';
import {filter} from 'rxjs/operators';
import {Permission} from '../../models/permission';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {
  PermissionPickDialogComponent,
  PermissionPickDialogComponentModule
} from '../permission-pick-dialog/permission-pick-dialog.component';

@Component({
  selector: 'app-permission-input',
  templateUrl: './permission-input.component.html',
  styleUrls: ['./permission-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => PermissionInputComponent),
    multi: true
  }]
})
export class PermissionInputComponent implements ControlValueAccessor {
  readonly dataSource = new MatTableDataSource<Permission>();
  onModelChange: Function;
  onModelTouched: Function;

  constructor(private dialog: MatDialog,
              private utilService: UtilService) {
  }

  pick(): void {
    PermissionPickDialogComponent.open(this.dialog, {permissions: this.dataSource.data})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  delete(permission: Permission): void {
    this.utilService.showConfirm()
      .subscribe(() => {
        const permissions = this.dataSource.data.filter(it => it.id !== permission.id);
        this.handleChange(permissions);
      });
  }

  handleChange(permissions: Permission[] = []): void {
    this.dataSource.data = permissions;
    this.onModelChange(permissions);
  }

  writeValue(permissions: Permission[] = []): void {
    this.dataSource.data = permissions;
  }

  setDisabledState(isDisabled: boolean): void {
  }

  registerOnChange(fn: any): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onModelTouched = fn;
  }
}

@NgModule({
  imports: [
    SharedModule,
    PermissionPickDialogComponentModule
  ],
  declarations: [
    PermissionInputComponent
  ],
  entryComponents: [],
  exports: [
    PermissionInputComponent
  ]
})
export class PermissionInputComponentModule {
}
