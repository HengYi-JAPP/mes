import {ChangeDetectionStrategy, Component, Input, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';
import {FormConfig} from '../../models/form-config';

@Component({
  selector: 'app-form-config-preview',
  templateUrl: './form-config-preview.component.html',
  styleUrls: ['./form-config-preview.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormConfigPreviewComponent implements OnDestroy {
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder) {
  }

  private _formConfig: FormConfig;

  @Input()
  get formConfig(): FormConfig {
    return this._formConfig;
  }

  set formConfig(value: FormConfig) {
    this._formConfig = FormConfig.assign(value);
    // this._formConfig = value;
    if (value) {
      this.updateFormGroup();
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private updateFormGroup() {

  }
}

