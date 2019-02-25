import {FocusMonitor} from '@angular/cdk/a11y';
import {coerceBooleanProperty} from '@angular/cdk/coercion';
import {
  ChangeDetectionStrategy,
  Component,
  DoCheck,
  ElementRef,
  HostBinding,
  Input,
  NgModule,
  OnDestroy,
  Optional,
  Self
} from '@angular/core';
import {AbstractControl, ControlValueAccessor, FormBuilder, FormGroup, NgControl} from '@angular/forms';
import {ValidationErrors} from '@angular/forms/src/directives/validators';
import {MatDialog, MatFormFieldControl} from '@angular/material';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, takeUntil} from 'rxjs/operators';
import {isNullOrUndefined} from 'util';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {SharedModule} from '../../shared.module';
import {
  BatchRangeValuesDialogComponent,
  BatchRangeValuesDialogComponentModule
} from '../batch-range-values-dialog/batch-range-values-dialog.component';

function matchData(start: string, end: string): { matchStart: RegExpMatchArray; prefixStart: string; numberStartString: string; numberStart: number; matchEnd: RegExpMatchArray; prefixEnd: string; numberEndString: string; numberEnd: number; } {
  const matchStart = isNullOrUndefined(start) ? null : start.match(/\d+$/);
  const prefixStart = isNullOrUndefined(matchStart) ? null : start.substring(0, matchStart.index);
  const numberStartString = isNullOrUndefined(matchStart) ? null : matchStart[0];
  const numberStart = isNullOrUndefined(numberStartString) ? null : parseInt(numberStartString, 10);

  const matchEnd = isNullOrUndefined(end) ? null : end.match(/\d+$/);
  const prefixEnd = isNullOrUndefined(matchEnd) ? null : end.substring(0, matchEnd.index);
  const numberEndString = isNullOrUndefined(matchEnd) ? null : matchEnd[0];
  const numberEnd = isNullOrUndefined(numberEndString) ? null : parseInt(numberEndString, 10);

  return {matchStart, prefixStart, numberStartString, numberStart, matchEnd, prefixEnd, numberEndString, numberEnd};
}

export class BatchRange {
  static ERROR_KEY = 'batchRangeError';
  start: string;
  end: string;

  get values(): string[] {
    const {prefixStart, numberStartString, numberStart, numberEnd} = matchData(this.start, this.end);
    const numberLength = numberStartString.length;
    const result = [];
    for (let i = numberStart; i <= numberEnd; i++) {
      const numValue = this.toFixLength(i + '', numberLength);
      const value = prefixStart + numValue;
      result.push(value);
    }
    return result;
  }

  validate(ctrl: AbstractControl): ValidationErrors | null {
    const {start, end} = ctrl.value;
    if (isNullOrUndefined(start) || isNullOrUndefined(end)) {
      return {[BatchRange.ERROR_KEY]: {translate: 'Validator.required'}};
    }
    const {matchStart, prefixStart, numberStart, numberStartString, matchEnd, prefixEnd, numberEnd, numberEndString} = matchData(start, end);
    if (isNullOrUndefined(matchStart) || isNullOrUndefined(matchEnd)) {
      // 没有数字可以进行批量
      return {[BatchRange.ERROR_KEY]: {translate: 'Validator.BatchRange.noNumberFind'}};
    }
    if (prefixStart !== prefixEnd) {
      // todo 根据最新输入，转化前缀，考虑提供后缀形式
      return {[BatchRange.ERROR_KEY]: {translate: 'Validator.BatchRange.prefix'}};
    }
    if (numberStartString.length !== numberEndString.length) {
      // todo 做成配置项， 数字位数不同
      return {[BatchRange.ERROR_KEY]: {translate: 'Validator.BatchRange.numberStringLength'}};
    }
    if (numberStart > numberEnd) {
      return {[BatchRange.ERROR_KEY]: {translate: 'Validator.BatchRange.startLargeThanEnd'}};
    }
    return null;
  }

  private toFixLength(numberString: string, length: number): string {
    const zeroCount = length - numberString.length;
    if (zeroCount <= 0) {
      return numberString;
    }
    for (let i = 0; i < zeroCount; i++) {
      numberString = '0' + numberString;
    }
    return numberString;
  }
}

@Component({
  selector: 'app-batch-range-input',
  templateUrl: './batch-range-input.component.html',
  styleUrls: ['./batch-range-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: MatFormFieldControl,
    useExisting: BatchRangeInputComponent
  }]
})
export class BatchRangeInputComponent implements ControlValueAccessor, MatFormFieldControl<BatchRange>, DoCheck, OnDestroy {
  static nextId = 0;
  @HostBinding() id = `app-batch-range-input-${BatchRangeInputComponent.nextId++}`;
  @HostBinding('attr.aria-describedby') describedBy = '';
  /** Implemented as part of MatFormFieldControl. */
  readonly stateChanges = new Subject<void>();
  /** The aria-describedby attribute on the input for improved a11y. */
  _ariaDescribedby: string;
  /** Implemented as part of MatFormFieldControl. */
  focused = false;
  /** Implemented as part of MatFormFieldControl. */
  controlType = 'app-batch-range-input';
  /** Implemented as part of MatFormFieldControl. */
  autofilled = false;
  errorState = false;
  readonly form: FormGroup;
  onModelChange: Function;
  onModelTouched: Function;
  private readonly batchRange = new BatchRange();

  constructor(@Optional() @Self() public ngControl: NgControl,
              private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private fm: FocusMonitor,
              private elRef: ElementRef) {
    if (this.ngControl != null) {
      this.ngControl.valueAccessor = this;
    }
    this.form = this.fb.group({
      'start': '',
      'end': ''
    }, {validator: this.batchRange.validate});
    this.start.valueChanges
      .pipe(
        takeUntil(this.stateChanges),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(() => {
        // const prefixStart = this.value.prefixStart;
        // const end = this.end.value;
      });
    this.fm.monitor(this.elRef.nativeElement, true)
      .subscribe(origin => {
        this.focused = !!origin;
        this.updateErrorState();
      });
    this.stateChanges
      .pipe(
        filter(() => !isNullOrUndefined(this.onModelChange))
      )
      .subscribe(() => {
        const batchRange = this.value;
        this.onModelChange(batchRange);
        this.ngControl.control.setErrors(batchRange.validate(this.form));
      });
  }

  get start() {
    return this.form.get('start');
  }

  get end() {
    return this.form.get('end');
  }

  @Input()
  get value(): BatchRange {
    Object.assign(this.batchRange, this.form.value);
    return this.batchRange;
  }

  set value(value: BatchRange) {
    this.form.setValue(value);
    this.updateErrorState();
  }

  /** Implemented as part of MatFormFieldControl. */
  private _disabled = false;

  @Input()
  get disabled() {
    if (this.ngControl && this.ngControl.disabled !== null) {
      return this.ngControl.disabled;
    }
    return this._disabled;
  }

  set disabled(value: boolean) {
    this._disabled = coerceBooleanProperty(value);
    // Browsers may not fire the blur event if the input is disabled too quickly.
    // Reset from here to ensure that the element doesn't become stuck.
    if (this.focused) {
      this.focused = false;
      this.updateErrorState();
    }
  }

  /** Implemented as part of MatFormFieldControl. */
  protected _required = false;

  @Input()
  get required() {
    return this._required;
  }

  set required(req) {
    this._required = coerceBooleanProperty(req);
    this.updateErrorState();
  }

  /** Implemented as part of MatFormFieldControl. */
  private _placeholder: string;

  @Input()
  get placeholder() {
    return this._placeholder;
  }

  set placeholder(plh) {
    this._placeholder = plh;
    this.updateErrorState();
  }

  @HostBinding('class.floating')
  get shouldLabelFloat() {
    return this.focused || !this.empty;
  }

  get empty() {
    const {start, end} = this.form.value;
    return !start && !end;
  }

  ngDoCheck(): void {
    if (this.ngControl != null) {
      this.updateErrorState();
    }
  }

  ngOnDestroy(): void {
    this.stateChanges.complete();
    this.fm.stopMonitoring(this.elRef.nativeElement);
  }

  showBatchRangeValues() {
    const batchRange = this.value;
    const errors = batchRange.validate(this.form);
    if (isNullOrUndefined(errors)) {
      BatchRangeValuesDialogComponent.open(this.dialog, {values: batchRange.values});
    } else {
      this.start.markAsTouched();
      this.end.markAsTouched();
      this.updateErrorState();
    }
  }

  writeValue(obj: any): void {
    this.form.reset();
    this.form.setValue({start: null, end: null});
    this.updateErrorState();
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    this.disabled ? this.form.disable() : this.form.enable();
    this.updateErrorState();
  }

  setTouched(): void {
  }

  registerOnChange(fn: any): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onModelTouched = fn;
  }

  updateErrorState() {
    if (this.form.disabled) {
      this.errorState = false;
      return this.stateChanges.next();
    }
    if (this.start.touched || this.end.touched) {
      this.errorState = this.form.invalid;
      return this.stateChanges.next();
    }
    if (this.form.pristine || this.start.pristine || this.end.pristine) {
      this.errorState = false;
      return this.stateChanges.next();
    }
    this.errorState = this.form.invalid;
    this.stateChanges.next();
  }

  setDescribedByIds(ids: string[]) {
    this.describedBy = ids.join(' ');
  }

  onContainerClick(ev: MouseEvent) {
    const tagName = (ev.target as Element).tagName.toLowerCase();
    if (tagName !== 'input' && tagName !== 'mat-icon') {
      this.elRef.nativeElement.querySelector('input').focus();
    }
  }

}

@NgModule({
  imports: [
    SharedModule,
    BatchRangeValuesDialogComponentModule
  ],
  declarations: [
    BatchRangeInputComponent
  ],
  entryComponents: [],
  exports: [
    BatchRangeInputComponent
  ]
})
export class BatchRangeInputComponentModule {
}
