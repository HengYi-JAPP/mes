import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormControl} from '@angular/forms';
import {MatDialog, PageEvent} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {of, Subject} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, filter, finalize, map, switchMap, take, takeUntil, tap} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {SilkCarUpdateDialogComponent} from '../../components/silk-car-update-dialog/silk-car-update-dialog.component';
import {SilkCar} from '../../models/silk-car';
import {ApiService} from '../../services/api.service';
import {PAGE_SIZE_OPTIONS, UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../../store/actions/core';
import {DeleteSuccess, SaveSuccess} from '../../store/actions/silk-car-manage-page';
import {
  silkCarManagePageCount,
  silkCarManagePagePageIndex,
  silkCarManagePagePageSize,
  silkCarManagePageQ,
  silkCarManagePageSilkCars
} from '../../store/config';

@Component({
  templateUrl: './silk-car-manage-page.component.html',
  styleUrls: ['./silk-car-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarManagePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-silk-car-manage-page') b2 = true;
  readonly displayedColumns = ['code', 'number', 'rowAndCol', 'type', 'btns'];
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly count$ = this.store.select(silkCarManagePageCount);
  readonly pageSize$ = this.store.select(silkCarManagePagePageSize);
  readonly pageIndex$ = this.store.select(silkCarManagePagePageIndex);
  readonly q$ = this.store.select(silkCarManagePageQ);
  readonly silkCars$ = this.store.select(silkCarManagePageSilkCars);
  readonly qCtrl = new FormControl();
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  ngOnInit(): void {
    this.qCtrl.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(q => {
        this.router.navigate(['config/silkCars'], {queryParams: {q}});
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  batchCreate() {
    SilkCarUpdateDialogComponent.batchCreate(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => it)
      )
      .subscribe(() => {
        this.router.navigate(['config/silkCars']);
        this.utilService.showSuccess();
      });
  }

  create() {
    this.update(SilkCar.assign());
  }

  update(silkCar: SilkCar) {
    SilkCarUpdateDialogComponent.open(this.dialog, {silkCar})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({silkCar: it}));
        this.utilService.showSuccess();
      });
  }

  delete(silkCar: SilkCar) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deleteSilkCar(silkCar.id)),
        map(() => new DeleteSuccess({id: silkCar.id})),
        tap(() => this.utilService.showSuccess()),
        catchError(error => of(new ShowError(error))),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(it => this.store.dispatch(it));
  }

  onPageEvent(ev: PageEvent) {
    const {pageIndex, pageSize} = ev;
    const first = pageIndex * pageSize;
    this.q$.pipe(take(1))
      .subscribe(q => {
        const queryParams = {first, pageSize, q};
        this.router.navigate(['config/silkCars'], {queryParams});
      });
  }

}
