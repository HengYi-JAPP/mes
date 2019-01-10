import {coerceBooleanProperty} from '@angular/cdk/coercion';
import {ChangeDetectionStrategy, Component, forwardRef, Input, NgModule} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {LineMachine} from '../../models/line-machine';
import {ApiService} from '../../services/api.service';
import {LineMachineCompare, UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {
  LineMachinePickDialogComponent,
  LineMachinePickDialogComponentModule
} from '../line-machine-pick-dialog/line-machine-pick-dialog.component';
import {LineMachineUpdateDialogComponentModule} from '../line-machine-update-dialog/line-machine-update-dialog.component';
import {SortDialogComponentModule} from '../sort-dialog/sort-dialog.component';

@Component({
  selector: 'app-line-machine-input',
  templateUrl: './line-machine-input.component.html',
  styleUrls: ['./line-machine-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => LineMachineInputComponent),
    multi: true
  }]
})
export class LineMachineInputComponent implements ControlValueAccessor {
  readonly values$ = new BehaviorSubject<LineMachine[]>([]);
  onModelChange: Function;
  onModelTouched: Function;

  constructor(private store: Store<any>,
              private dialog: MatDialog,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  private _required = false;

  @Input()
  get required() {
    return this._required;
  }

  set required(req) {
    this._required = coerceBooleanProperty(req);
  }

  private _multi = true;

  @Input()
  get multi(): boolean {
    return this._multi;
  }

  set multi(value: boolean) {
    this._multi = coerceBooleanProperty(value);
  }

  pick() {
    LineMachinePickDialogComponent.open(this.dialog, {dest: this.values$.value})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  handleChange(value: LineMachine[] = []): void {
    const next = (value || []).sort(LineMachineCompare);
    this.values$.next(next);
    this.onModelChange(next);
  }

  writeValue(value: LineMachine[]): void {
    const next = (value || []).sort(LineMachineCompare);
    this.values$.next(next);
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
    SortDialogComponentModule,
    LineMachinePickDialogComponentModule,
    LineMachineUpdateDialogComponentModule
  ],
  declarations: [
    LineMachineInputComponent
  ],
  exports: [
    LineMachineInputComponent
  ]
})
export class LineMachineInputComponentModule {
}
