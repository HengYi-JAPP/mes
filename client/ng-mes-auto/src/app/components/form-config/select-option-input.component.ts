import {ChangeDetectionStrategy, Component, forwardRef} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {UtilService} from '../../services/util.service';
import {SelectOptionUpdateDialogComponent} from './select-option-update-dialog.component';

@Component({
  selector: 'app-selection-value-input',
  templateUrl: './select-option-input.component.html',
  styleUrls: ['./select-option-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SelectOptionInputComponent),
    multi: true
  }]
})
export class SelectOptionInputComponent implements ControlValueAccessor {
  readonly selectOptions$ = new BehaviorSubject<string[]>([]);
  private onModelChange: Function;
  private onModelTouched: Function;

  constructor(private dialog: MatDialog,
              private utilService: UtilService) {
  }

  get selectOptions() {
    return this.selectOptions$.value || [];
  }

  create() {
    SelectOptionUpdateDialogComponent.create(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        const next = [...this.selectOptions];
        next.push(it);
        this.handleChange(next);
      });
  }

  update(selectOption: string) {
    SelectOptionUpdateDialogComponent.update(this.dialog, {selectOption})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(newSelectOption => {
        const next = this.selectOptions.map(it => {
          if (it === selectOption) {
            return newSelectOption;
          }
          return it;
        });
        this.handleChange(next);
      });
  }

  delete(selectOption: string) {
    this.utilService.showConfirm()
      .subscribe(() => {
        const next = this.selectOptions.filter(it => it !== selectOption);
        this.handleChange(next);
      });
  }

  handleChange(value: string[]): void {
    this.selectOptions$.next(value);
    this.onModelChange(value);
  }

  writeValue(value: string[]): void {
    this.selectOptions$.next(value);
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
