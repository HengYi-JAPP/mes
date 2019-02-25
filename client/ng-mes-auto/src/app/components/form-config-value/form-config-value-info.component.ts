import {ChangeDetectionStrategy, Component, Input, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {BehaviorSubject, Subject} from 'rxjs';
import {FormConfig} from '../../models/form-config';

@Component({
  selector: 'app-form-config-value-info',
  templateUrl: './form-config-value-info.component.html',
  styleUrls: ['./form-config-value-info.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormConfigValueInfoComponent implements OnDestroy {

  readonly values$ = new BehaviorSubject<any>(null);
  private readonly _destroy$ = new Subject();

  constructor(private fb: FormBuilder) {
  }

  private _formConfigValueData: any;
  @Input()
  get formConfigValueData(): any {
    return this._formConfigValueData;
  }

  set formConfigValueData(value: any) {
    this._formConfigValueData = Object.assign({}, value);
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

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

}
