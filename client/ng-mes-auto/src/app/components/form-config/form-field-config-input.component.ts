import {ChangeDetectionStrategy, Component, forwardRef} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {FormFieldConfig} from '../../models/form-field-config';
import {UtilService} from '../../services/util.service';
import {FormFieldConfigUpdateDialogComponent} from './form-field-config-update-dialog.component';

@Component({
  selector: 'app-form-field-config-input',
  templateUrl: './form-field-config-input.component.html',
  styleUrls: ['./form-field-config-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => FormFieldConfigInputComponent),
    multi: true
  }]
})
export class FormFieldConfigInputComponent implements ControlValueAccessor {
  readonly formFieldConfigs$ = new BehaviorSubject<FormFieldConfig[]>([]);
  private onModelChange: Function;
  private onModelTouched: Function;

  constructor(private dialog: MatDialog,
              private utilService: UtilService) {
  }

  get formFieldConfigs() {
    return this.formFieldConfigs$.value || [];
  }

  create() {
    FormFieldConfigUpdateDialogComponent.create(this.dialog, {formFieldConfig: FormFieldConfig.assign()})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        const next = [...this.formFieldConfigs];
        next.push(it);
        this.handleChange(next);
      });
  }

  update(formFieldConfig: FormFieldConfig) {
    FormFieldConfigUpdateDialogComponent.update(this.dialog, {formFieldConfig})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(newFormFieldConfig => {
        const next = this.formFieldConfigs.map(it => {
          if (it === formFieldConfig) {
            return newFormFieldConfig;
          }
          return it;
        });
        this.handleChange(next);
      });
  }

  delete(formFieldConfig: FormFieldConfig) {
    this.utilService.showConfirm()
      .subscribe(() => {
        const next = this.formFieldConfigs.filter(it => it !== formFieldConfig);
        this.handleChange(next);
      });
  }

  handleChange(value: FormFieldConfig[]): void {
    this.formFieldConfigs$.next(value);
    this.onModelChange(value);
  }

  writeValue(value: FormFieldConfig[]): void {
    this.formFieldConfigs$.next(value);
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
