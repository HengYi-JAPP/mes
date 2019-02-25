import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog, MatPaginator, PageEvent} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {of, Subject} from 'rxjs';
import {catchError, debounceTime, distinctUntilChanged, filter, finalize, map, switchMap, take, takeUntil, tap} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {BatchUpdateDialogComponent} from '../../components/batch-update-dialog/batch-update-dialog.component';
import {Batch} from '../../models/batch';
import {ApiService} from '../../services/api.service';
import {PAGE_SIZE_OPTIONS, UtilService} from '../../services/util.service';
import {DeleteSuccess, SaveSuccess} from '../../store/actions/batch-manage-page';
import {SetLoading, ShowError} from '../../store/actions/core';
import {
  batchManagePageBatches,
  batchManagePageCount,
  batchManagePagePageIndex,
  batchManagePagePageSize,
  batchManagePageQ
} from '../../store/product-plan';

@Component({
  templateUrl: './batch-manage-page.component.html',
  styleUrls: ['./batch-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BatchManagePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-batch-manage-page') b2 = true;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  readonly displayedColumns = ['workshop', 'product', 'batchNo', 'spec', 'silkWeight', 'tubeColor', 'note', 'btns'];
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly searchForm: FormGroup = this.fb.group({q: ''});
  readonly batches$ = this.store.select(batchManagePageBatches);
  readonly count$ = this.store.select(batchManagePageCount);
  readonly pageSize$ = this.store.select(batchManagePagePageSize);
  readonly pageIndex$ = this.store.select(batchManagePagePageIndex);
  readonly q$ = this.store.select(batchManagePageQ);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  ngOnInit(): void {
    this.searchForm.get('q').valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(q => {
        this.router.navigate(['productPlan/batches'], {queryParams: {q}});
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    this.update(Batch.assign());
  }

  update(batch: Batch) {
    BatchUpdateDialogComponent.open(this.dialog, {batch})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({batch: it}));
        this.utilService.showSuccess();
      });
  }

  delete(batch: Batch) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deleteBatch(batch.id)),
        map(() => new DeleteSuccess({id: batch.id})),
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
        this.router.navigate(['productPlan/batches'], {queryParams: {first, pageSize, q}});
      });
  }

}
