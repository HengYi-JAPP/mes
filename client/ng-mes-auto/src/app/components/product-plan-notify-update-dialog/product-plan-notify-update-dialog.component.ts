import {HttpParams} from '@angular/common/http';
import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {Observable, Subject} from 'rxjs';
import {debounceTime, delay, distinctUntilChanged, filter, finalize, map, switchMap, takeUntil} from 'rxjs/operators';
import {isString} from 'util';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {Batch} from '../../models/batch';
import {ProductPlanNotify} from '../../models/product-plan-notify';
import {ApiService} from '../../services/api.service';
import {requiredPickEntity} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SetLoading, ShowError} from '../../store/actions/core';
import {coreLoading} from '../../store/core';
import {LineMachineInputComponentModule} from '../line-machine-input/line-machine-input.component';

@Component({
  templateUrl: './product-plan-notify-update-dialog.component.html',
  styleUrls: ['./product-plan-notify-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPlanNotifyUpdateDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-product-plan-notify-update-dialog') b2 = true;
  readonly coreLoading$ = this.store.select(coreLoading);
  readonly dialogTitle: string;
  readonly form: FormGroup;
  batches$: Observable<Batch[]>;
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<ProductPlanNotifyUpdateDialogComponent, ProductPlanNotify>,
              @Inject(MAT_DIALOG_DATA)  data: { productPlanNotify: ProductPlanNotify }) {
    const {productPlanNotify} = data;
    this.dialogTitle = productPlanNotify.id ? 'Common.edit' : 'Common.create';
    this.form = this.fb.group({
      id: productPlanNotify.id,
      startDate: [moment(productPlanNotify.startDate), Validators.required],
      batch: [productPlanNotify.batch, [Validators.required, requiredPickEntity]],
      type: [productPlanNotify.type || 'CHANGE_BATCH', Validators.required],
      lineMachines: [productPlanNotify.lineMachines, [Validators.required, Validators.minLength(1)]],
      name: [productPlanNotify.name, Validators.required]
    });
  }

  get lineMachines() {
    return this.form.get('lineMachines');
  }

  get startDate() {
    return this.form.get('startDate');
  }

  get batch() {
    return this.form.get('batch');
  }

  get type() {
    return this.form.get('type');
  }

  get name() {
    return this.form.get('name');
  }

  static open(dialog: MatDialog, data: { productPlanNotify: ProductPlanNotify }): MatDialogRef<ProductPlanNotifyUpdateDialogComponent, ProductPlanNotify> {
    return dialog.open(ProductPlanNotifyUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  ngOnInit(): void {
    this.batches$ = this.batch.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged(),
        filter(it => it && isString(it) && it.trim().length > 0),
        switchMap(q => {
          const params = new HttpParams().set('pageSize', '10').set('q', q);
          return this.apiService.listBatch(params);
        }),
        map(({batches}) => batches)
      );
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  displayWithBatch(batch: Batch): string {
    return batch && batch.batchNo;
  }

  submit() {
    this.store.dispatch(new SetLoading());
    this.apiService.saveProductPlanNotify(this.form.value)
      .pipe(
        delay(3000),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}


@NgModule({
  imports: [
    SharedModule,
    LineMachineInputComponentModule
  ],
  declarations: [
    ProductPlanNotifyUpdateDialogComponent
  ],
  entryComponents: [
    ProductPlanNotifyUpdateDialogComponent
  ],
  exports: [
    ProductPlanNotifyUpdateDialogComponent
  ]
})
export class ProductPlanNotifyUpdateDialogComponentModule {
}
