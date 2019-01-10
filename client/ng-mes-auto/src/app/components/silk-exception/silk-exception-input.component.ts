import {coerceBooleanProperty, coerceNumberProperty} from '@angular/cdk/coercion';
import {ChangeDetectionStrategy, Component, forwardRef, Input, NgModule, OnInit} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {SilkException} from '../../models/silk-exception';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SortDialogComponent, SortDialogComponentModule} from '../sort-dialog/sort-dialog.component';
import {SilkExceptionPickDialogComponent} from './silk-exception-pick-dialog.component';
import {SilkExceptionUpdateDialogComponent} from './silk-exception-update-dialog.component';

@Component({
  selector: 'app-silk-exception-input',
  templateUrl: './silk-exception-input.component.html',
  styleUrls: ['./silk-exception-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SilkExceptionInputComponent),
    multi: true
  }]
})
export class SilkExceptionInputComponent implements ControlValueAccessor, OnInit {
  readonly silkExceptions$ = new BehaviorSubject<SilkException[]>([]);
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

  private _min: number;

  @Input()
  get min(): number {
    return !this.required ? 0 : Math.max(1, this._min);
  }

  set min(min: number) {
    this._min = coerceNumberProperty(min);
  }

  ngOnInit(): void {
  }

  sort() {
    SortDialogComponent.open<SilkException>(this.dialog, {datas: this.silkExceptions$.value, displayKeys: 'name'})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  pick() {
    SilkExceptionPickDialogComponent.open(this.dialog, {silkExceptions: this.silkExceptions$.value})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  update(silkException: SilkException) {
    SilkExceptionUpdateDialogComponent.open(this.dialog, {silkException})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(newSilkException => {
        const silkExceptions = this.silkExceptions$.value
          .map(it => it.id === newSilkException.id ? newSilkException : it);
        this.handleChange(silkExceptions);
        this.utilService.showSuccess();
      });
  }

  handleChange(silkExceptions: SilkException[] = []): void {
    this.silkExceptions$.next(silkExceptions);
    this.onModelChange(silkExceptions);
  }

  writeValue(silkExceptions: SilkException[] = []): void {
    this.silkExceptions$.next(silkExceptions);
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
    SortDialogComponentModule
  ],
  declarations: [
    SilkExceptionInputComponent,
    SilkExceptionPickDialogComponent,
    SilkExceptionUpdateDialogComponent
  ],
  entryComponents: [
    SilkExceptionPickDialogComponent,
    SilkExceptionUpdateDialogComponent
  ],
  exports: [
    SilkExceptionInputComponent,
    SilkExceptionPickDialogComponent,
    SilkExceptionUpdateDialogComponent
  ]
})
export class SilkExceptionModule {
}
