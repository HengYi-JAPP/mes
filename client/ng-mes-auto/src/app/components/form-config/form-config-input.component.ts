import {ChangeDetectionStrategy, Component, forwardRef, NgModule} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {FormConfig} from '../../models/form-config';
import {SharedModule} from '../../shared.module';
import {FormConfigPickDialogComponent} from './form-config-pick-dialog.component';
import {FormConfigPreviewDialogComponent} from './form-config-preview-dialog.component';
import {FormConfigPreviewComponent} from './form-config-preview.component';
import {FormConfigUpdateDialogComponent} from './form-config-update-dialog.component';
import {FormFieldConfigInputComponent} from './form-field-config-input.component';
import {FormFieldConfigUpdateDialogComponent} from './form-field-config-update-dialog.component';
import {SelectOptionInputComponent} from './select-option-input.component';
import {SelectOptionUpdateDialogComponent} from './select-option-update-dialog.component';

@Component({
  selector: 'app-form-config-input',
  templateUrl: './form-config-input.component.html',
  styleUrls: ['./form-config-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => FormConfigInputComponent),
    multi: true
  }]
})
export class FormConfigInputComponent implements ControlValueAccessor {
  readonly formConfig$ = new BehaviorSubject<FormConfig>(null);
  private onModelChange: Function;
  private onModelTouched: Function;

  constructor(private dialog: MatDialog) {
  }

  get formConfig() {
    return this.formConfig$.value;
  }

  preview(): void {
    FormConfigPreviewDialogComponent.open(this.dialog, {formConfig: this.formConfig});
  }

  pick(): void {
    FormConfigPickDialogComponent.open(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  handleChange(value: FormConfig): void {
    this.formConfig$.next(value);
    this.onModelChange(value);
  }

  writeValue(value: FormConfig): void {
    this.formConfig$.next(value);
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
    SharedModule
  ],
  declarations: [
    FormConfigInputComponent,
    FormConfigPickDialogComponent,
    FormConfigUpdateDialogComponent,
    FormFieldConfigInputComponent,
    FormFieldConfigUpdateDialogComponent,
    SelectOptionInputComponent,
    SelectOptionUpdateDialogComponent,
    FormConfigPreviewComponent,
    FormConfigPreviewDialogComponent
  ],
  entryComponents: [
    FormConfigPickDialogComponent,
    FormConfigUpdateDialogComponent,
    FormFieldConfigUpdateDialogComponent,
    SelectOptionUpdateDialogComponent,
    FormConfigPreviewDialogComponent
  ],
  exports: [
    FormConfigInputComponent,
    FormConfigPickDialogComponent,
    FormConfigUpdateDialogComponent,
    FormFieldConfigInputComponent,
    FormFieldConfigUpdateDialogComponent,
    SelectOptionInputComponent,
    SelectOptionUpdateDialogComponent,
    FormConfigPreviewComponent,
    FormConfigPreviewDialogComponent
  ]
})
export class FormConfigModule {
}
