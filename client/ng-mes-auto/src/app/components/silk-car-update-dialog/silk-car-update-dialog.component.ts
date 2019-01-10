import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, OnDestroy} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {from, Subject} from 'rxjs';
import {filter, map, switchMap, takeUntil, toArray, withLatestFrom} from 'rxjs/operators';
import {SilkCar} from '../../models/silk-car';
import {ApiService} from '../../services/api.service';
import {Storage} from '../../services/storage';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {BatchRangeInputComponentModule} from '../batch-range-input/batch-range-input.component';

const BATCH_MODEL = 'BATCH_MODEL';

@Component({
  templateUrl: './silk-car-update-dialog.component.html',
  styleUrls: ['./silk-car-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarUpdateDialogComponent implements OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-silk-car-update-dialog') b2 = true;
  readonly batchModel: boolean;
  readonly corporationCtrl = new FormControl();
  dialogTitle: string;
  form: FormGroup;
  private readonly _destroy$ = new Subject();
  private readonly silkCarKey = 'SilkCarUpdateDialogComponent.silkCar';

  constructor(private store: Store<any>,
              private storage: Storage,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<SilkCarUpdateDialogComponent, SilkCar | boolean>,
              @Inject(MAT_DIALOG_DATA)  data: { model: string, silkCar: SilkCar }) {
    this.batchModel = data.model === BATCH_MODEL;
    this.batchModel ? this.batchInit() : this.init(data.silkCar);
    this.apiService.listCorporation()
      .subscribe(it => this.corporationCtrl.setValue(it[0]));
    this.dialogRef.afterClosed()
      .pipe(
        filter(it => it && !this.batchModel)
      )
      .subscribe(it => this.storage.setItem(this.silkCarKey, it));
  }

  get batchRange() {
    return this.form.get('batchRange');
  }

  get type() {
    return this.form.get('type');
  }

  get number() {
    return this.form.get('number');
  }

  get code() {
    return this.form.get('code');
  }

  get row() {
    return this.form.get('row');
  }

  get col() {
    return this.form.get('col');
  }

  private get preSilkCar(): SilkCar {
    return SilkCar.assign(this.storage.getItem(this.silkCarKey));
  }

  static open(dialog: MatDialog, data: { silkCar: SilkCar }): MatDialogRef<SilkCarUpdateDialogComponent, SilkCar> {
    return dialog.open(SilkCarUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  static batchCreate(dialog: MatDialog): MatDialogRef<SilkCarUpdateDialogComponent, boolean> {
    const data = {model: BATCH_MODEL};
    return dialog.open(SilkCarUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  submit() {
    this.apiService.saveSilkCar(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  batchSubmit() {
    const formValue = this.form.value;
    const {batchRange} = formValue;
    delete formValue.batchRange;
    from(batchRange.values)
      .pipe(
        map(number => {
          const code = this.corporationCtrl.value.code + number;
          return {...formValue, number, code};
        }),
        toArray(),
        switchMap(it => this.apiService.batchSilkCars(it))
      )
      .subscribe(
        () => this.dialogRef.close(true),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  private init(silkCar: SilkCar) {
    this.dialogTitle = silkCar.id ? 'Common.edit' : 'Common.create';
    const {type, row, col} = this.preSilkCar;
    this.form = this.fb.group({
      id: silkCar.id,
      type: [silkCar.type || type || 'DEFAULT', Validators.required],
      number: [silkCar.number, Validators.required],
      code: [silkCar.code, Validators.required],
      row: [silkCar.row || row || 3, [Validators.required, Validators.min(3)]],
      col: [silkCar.col || col || 4, [Validators.required, Validators.min(4)]]
    });
    this.number.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        withLatestFrom(this.corporationCtrl.valueChanges)
      )
      .subscribe(([number, corporation]) => {
        if (number) {
          this.code.setValue(corporation.code + number);
        } else {
          this.code.setValue('');
        }
      });
  }

  private batchInit() {
    this.dialogTitle = 'Common.batch';
    const {type, row, col} = this.preSilkCar;
    this.form = this.fb.group({
      batchRange: [null, Validators.required],
      type: [type || 'DEFAULT', Validators.required],
      row: [row || 1, [Validators.required, Validators.min(1)]],
      col: [col || 1, [Validators.required, Validators.min(1)]]
    });
  }

}


@NgModule({
  imports: [
    SharedModule,
    BatchRangeInputComponentModule
  ],
  declarations: [
    SilkCarUpdateDialogComponent
  ],
  entryComponents: [
    SilkCarUpdateDialogComponent
  ],
  exports: [
    SilkCarUpdateDialogComponent
  ]
})
export class SilkCarUpdateDialogComponentModule {
}
