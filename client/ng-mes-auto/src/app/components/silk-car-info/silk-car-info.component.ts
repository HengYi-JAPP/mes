import {SelectionModel} from '@angular/cdk/collections';
import {ChangeDetectionStrategy, Component, ElementRef, Input, NgModule, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {createSelector, Store} from '@ngrx/store';
import {BehaviorSubject, Subject} from 'rxjs';
import {filter, finalize, map, switchMap, take, takeUntil, tap} from 'rxjs/operators';
import {ProductProcess} from '../../models/product-process';
import {SilkCar} from '../../models/silk-car';
import {SilkCarRecord} from '../../models/silk-car-record';
import {SilkCarRuntime} from '../../models/silk-car-runtime';
import {SilkRuntime} from '../../models/silk-runtime';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SetLoading, ShowError} from '../../store/actions/core';
import {
  DyeingSampleSilkSubmitDialogComponent,
  DyeingSampleSilkSubmitDialogComponentModule
} from '../dyeing-sample-silk-submit-dialog/dyeing-sample-silk-submit-dialog.component';
import {
  ProductProcessSubmitDialogComponent,
  ProductProcessSubmitDialogComponentModule
} from '../product-process-submit-dialog/product-process-submit-dialog.component';


interface SilkCarInfoState {
  // model?: 'RUNTIME' | 'HISTORY';
  model?: string;

  silkCarRecord?: SilkCarRecord;
  productProcesses?: ProductProcess[];
  silkRuntimes?: SilkRuntime[];
}

function calcSideSilkRuntimes(silkCar: SilkCar, silkRuntimes: SilkRuntime[], sideType: string): SilkRuntime[] {
  if (!silkCar) {
    return [];
  }
  const sideSilkRuntime: SilkRuntime[] = [];
  const rowCount = silkCar && silkCar.row || 0;
  const colCount = silkCar && silkCar.col || 0;
  for (let row = 1; row <= rowCount; row++) {
    for (let col = 1; col <= colCount; col++) {
      const find = (silkRuntimes || []).find(it => it.sideType === sideType && it.row === row && it.col === col);
      const silkRuntime = find ? find : SilkRuntime.assign({sideType, row, col});
      sideSilkRuntime.push(silkRuntime);
    }
  }
  return sideSilkRuntime;
}

const getModel = (state: SilkCarInfoState) => state.model;
const getSilkCarRecord = (state: SilkCarInfoState) => state.silkCarRecord;
const getProductProcesses = (state: SilkCarInfoState) => state.productProcesses;
const getSilkRuntimes = (state: SilkCarInfoState) => (state.silkRuntimes || []).map(it => SilkRuntime.assign(it));
const getSilkCar = createSelector(getSilkCarRecord, it => it && it.silkCar);
const getBatch = createSelector(getSilkCarRecord, it => it && it.batch);
const getDoffingType = createSelector(getSilkCarRecord, it => it && it.doffingType);

const getASideSilkRuntimes = createSelector(getSilkCar, getSilkRuntimes, (silkCar, silkRuntimes) => calcSideSilkRuntimes(silkCar, silkRuntimes, 'A'));
const getBSideSilkRuntimes = createSelector(getSilkCar, getSilkRuntimes, (silkCar, silkRuntimes) => calcSideSilkRuntimes(silkCar, silkRuntimes, 'B'));

const getRow = createSelector(getSilkCar, it => it && it.row || 0);
const getCol = createSelector(getSilkCar, it => it && it.col || 0);
const getStyleHeight = createSelector(getRow, row => (row * 100 + 2) + 'px');

@Component({
  selector: 'app-silk-car-info',
  templateUrl: './silk-car-info.component.html',
  styleUrls: ['./silk-car-info.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarInfoComponent implements OnDestroy {
  readonly aSideSelection = new SelectionModel<string>(true, []);
  readonly bSideSelection = new SelectionModel<string>(true, []);
  private readonly _destroy$ = new Subject();
  private readonly state$ = new BehaviorSubject<SilkCarInfoState>({});
  readonly model$ = this.state$.pipe(map(getModel));
  readonly silkCarRecord$ = this.state$.pipe(map(getSilkCarRecord));
  readonly doffingType$ = this.state$.pipe(map(getDoffingType));
  readonly productProcesses$ = this.state$.pipe(map(getProductProcesses));
  readonly silkCar$ = this.state$.pipe(map(getSilkCar));
  readonly batch$ = this.state$.pipe(map(getBatch));
  readonly aSideSilkRuntimes$ = this.state$.pipe(map(getASideSilkRuntimes));
  readonly bSideSilkRuntimes$ = this.state$.pipe(map(getBSideSilkRuntimes));
  readonly styleHeight$ = this.state$.pipe(map(getStyleHeight));
  readonly col$ = this.state$.pipe(map(getCol));

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private elRef: ElementRef,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  @Input()
  set silkCarRuntime(silkCarRuntime: SilkCarRuntime) {
    this.aSideSelection.clear();
    this.bSideSelection.clear();
    if (!silkCarRuntime) {
      this.state$.next({});
      return;
    }
    const {silkCarRecord: {batch: {product}}} = silkCarRuntime;
    this.apiService.getProduct_ProductProcess(product.id)
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        // TODO 菜单权限过滤
        map(productProcesses => {
          const {silkCarRecord, silkRuntimes} = silkCarRuntime;
          return {model: 'RUNTIME', silkCarRuntime, silkCarRecord, silkRuntimes, productProcesses};
        }),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(
        it => this.state$.next(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  @Input()
  set silkCarHistory(silkCarRecord: SilkCarRecord) {
    if (!silkCarRecord) {
      this.state$.next({});
      return;
    }
    this.state$.next({model: 'RUNTIME', silkCarRecord});
  }

  get aSideAllSelected(): boolean {
    const numSelected = this.aSideSelection.selected.length;
    const {silkRuntimes} = this.state$.value;
    const numRows = silkRuntimes ? silkRuntimes.filter(it => it.sideType === 'A').length : 0;
    return numSelected === numRows;
  }

  get bSideAllSelected(): boolean {
    const numSelected = this.bSideSelection.selected.length;
    const {silkRuntimes} = this.state$.value;
    const numRows = silkRuntimes ? silkRuntimes.filter(it => it.sideType === 'B').length : 0;
    return numSelected === numRows;
  }

  get selectedSilkRuntimes(): SilkRuntime[] {
    return this.aSideSelection.selected.concat(this.bSideSelection.selected)
      .map(silkId => {
        const {silkRuntimes} = this.state$.value;
        const find = silkRuntimes.find(it => it.silk.id === silkId);
        return SilkRuntime.assign(find);
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  productProcessSubmit(productProcess: ProductProcess) {
    this.silkCarRecord$
      .pipe(
        takeUntil(this._destroy$),
        take(1),
        filter(it => !!it),
        map(silkCarRecord => ({silkCarRecord, silkRuntimes: this.selectedSilkRuntimes, productProcess})),
        switchMap(it => ProductProcessSubmitDialogComponent.open(this.dialog, it).afterClosed())
      )
      .subscribe();
  }

  dyeingSampleSilkSubmit() {
    this.silkCarRecord$
      .pipe(
        takeUntil(this._destroy$),
        take(1),
        filter(it => !!it),
        map(silkCarRecord => ({silkCarRecord, silkRuntimes: this.selectedSilkRuntimes})),
        switchMap(it => DyeingSampleSilkSubmitDialogComponent.open(this.dialog, it).afterClosed())
      )
      .subscribe();
  }

  aSideMasterToggle() {
    if (this.aSideAllSelected) {
      this.aSideSelection.clear();
      return;
    }
    const {silkRuntimes} = this.state$.value;
    (silkRuntimes || []).filter(it => it.sideType === 'A')
      .map(it => it.silk.id)
      .forEach(it => this.aSideSelection.select(it));
  }

  bSideMasterToggle() {
    if (this.bSideAllSelected) {
      this.bSideSelection.clear();
      return;
    }
    const {silkRuntimes} = this.state$.value;
    (silkRuntimes || []).filter(it => it.sideType === 'B')
      .map(it => it.silk.id)
      .forEach(it => this.bSideSelection.select(it));
  }

}

@NgModule({
  imports: [
    SharedModule,
    ProductProcessSubmitDialogComponentModule,
    DyeingSampleSilkSubmitDialogComponentModule
  ],
  declarations: [
    SilkCarInfoComponent
  ],
  exports: [
    SilkCarInfoComponent
  ]
})
export class SilkCarInfoComponentModule {
}
