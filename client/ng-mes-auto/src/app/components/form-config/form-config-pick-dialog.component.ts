import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap, takeUntil} from 'rxjs/operators';
import {isString} from 'util';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {FormConfig} from '../../models/form-config';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {FormConfigUpdateDialogComponent} from './form-config-update-dialog.component';

@Component({
  templateUrl: './form-config-pick-dialog.component.html',
  styleUrls: ['./form-config-pick-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class FormConfigPickDialogComponent implements OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-form-config-pick-dialog') b2 = true;
  readonly dialogTitle = 'FormConfig.pick';
  readonly qCtrl = new FormControl();
  private readonly _destroy$ = new Subject();
  readonly formConfigs$ = this.qCtrl.valueChanges
    .pipe(
      takeUntil(this._destroy$),
      debounceTime(SEARCH_DEBOUNCE_TIME),
      distinctUntilChanged(),
      filter(it => it && isString(it) && it.trim().length > 0),
      switchMap(q => this.apiService.autoCompleteFormConfig(q))
    );

  constructor(private dialog: MatDialog,
              private dialogRef: MatDialogRef<FormConfigPickDialogComponent, FormConfig>,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  static open(dialog: MatDialog): MatDialogRef<FormConfigPickDialogComponent, FormConfig> {
    return dialog.open(FormConfigPickDialogComponent, {panelClass: 'my-dialog', disableClose: true});
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  preview(formConfig: FormConfig) {
    this.update(FormConfig.assign());
  }

  create() {
    this.update(FormConfig.assign());
  }

  update(formConfig: FormConfig) {
    FormConfigUpdateDialogComponent.open(this.dialog, {formConfig})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.submit(it);
        this.utilService.showSuccess();
      });
  }

  submit(value: FormConfig) {
    this.dialogRef.close(value);
  }
}
