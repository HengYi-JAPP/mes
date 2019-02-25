import {ChangeDetectionStrategy, Component, forwardRef, NgModule} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog, MatTableDataSource} from '@angular/material';
import {OperatorGroup} from '../../models/operator-group';
import {SharedModule} from '../../shared.module';
import {PermissionPickDialogComponentModule} from '../permission-pick-dialog/permission-pick-dialog.component';

@Component({
  selector: 'app-operator-group-input',
  templateUrl: './operator-group-input.component.html',
  styleUrls: ['./operator-group-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => OperatorGroupInputComponent),
    multi: true
  }]
})
export class OperatorGroupInputComponent implements ControlValueAccessor {
  readonly dataSource = new MatTableDataSource<OperatorGroup>();
  onModelChange: Function;
  onModelTouched: Function;

  constructor(private dialog: MatDialog) {
  }

  pick(): void {
    // PermissionPickDialogComponent.open(this.dialog, {permissions: this.dataSource.data})
    //   .afterClosed()
    //   .pipe(
    //     filter(it => !!it)
    //   )
    //   .subscribe(it => this.handleChange(it));
  }

  handleChange(permissions: OperatorGroup[]): void {
    this.dataSource.data = permissions;
    this.onModelChange(permissions);
  }

  writeValue(permissions: OperatorGroup[]): void {
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
    OperatorGroupInputComponent
  ],
  entryComponents: [],
  exports: [
    OperatorGroupInputComponent
  ]
})
export class OperatorGroupInputComponentModule {
}
