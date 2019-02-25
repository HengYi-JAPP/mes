import {ChangeDetectionStrategy, Component, forwardRef, Input, NgModule, OnChanges, OnDestroy, SimpleChanges} from '@angular/core';
import {ControlValueAccessor, FormBuilder, FormControl, FormGroup, NG_VALUE_ACCESSOR} from '@angular/forms';
import {BehaviorSubject, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import {FormConfig} from '../../models/form-config';
import {SharedModule} from '../../shared.module';
import {FormConfigValueInfoComponent} from './form-config-value-info.component';

@Component({
  selector: 'app-form-config-value-input',
  templateUrl: './form-config-value-input.component.html',
  styleUrls: ['./form-config-value-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => FormConfigValueInputComponent),
    multi: true
  }]
})
export class FormConfigValueInputComponent implements ControlValueAccessor, OnDestroy, OnChanges {
  readonly values$ = new BehaviorSubject<any>(null);
  form: FormGroup;
  onModelChange: Function;
  onModelTouched: Function;
  private readonly _destroy$ = new Subject();

  constructor(private fb: FormBuilder) {
  }

  private _formConfig: FormConfig;

  @Input()
  get formConfig(): FormConfig {
    return this._formConfig;
  }

  set formConfig(value: FormConfig) {
    if (value) {
      this._formConfig = FormConfig.assign(value);
    } else {
      this._formConfig = value;
    }
  }

  handleChange(value: any): void {
    this.values$.next(value);
    this.onModelChange(value);
  }

  writeValue(value: any): void {
    this.values$.next(value);
  }

  setDisabledState(isDisabled: boolean): void {
  }

  registerOnChange(fn: any): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onModelTouched = fn;
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }


  ngOnChanges(changes: SimpleChanges): void {
    if (changes.formConfig && changes.formConfig.currentValue) {
      this.form = this.fb.group({});
      this.formConfig.formFieldConfigs.forEach(it => {
        const ctrl = new FormControl();
        this.form.registerControl(it.id, ctrl);
      });
      this.form.valueChanges
        .pipe(
          takeUntil(this._destroy$)
        )
        .subscribe(it => this.handleChange(it));
    }
  }

}

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    FormConfigValueInputComponent,
    FormConfigValueInfoComponent
  ],
  entryComponents: [],
  exports: [
    FormConfigValueInputComponent,
    FormConfigValueInfoComponent
  ]
})
export class FormConfigValueModule {
}
