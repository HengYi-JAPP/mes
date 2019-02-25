import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog, PageEvent} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {Observable, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, take, takeUntil} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {ProductPlanNotifyUpdateDialogComponent} from '../../components/product-plan-notify-update-dialog/product-plan-notify-update-dialog.component';
import {ProductPlanNotify} from '../../models/product-plan-notify';
import {ApiService} from '../../services/api.service';
import {PAGE_SIZE_OPTIONS, UtilService} from '../../services/util.service';
import {SaveSuccess} from '../../store/actions/product-plan-notify-manage-page';
import {
  productPlanNotifyManagePageCount,
  productPlanNotifyManagePagePageIndex,
  productPlanNotifyManagePagePageSize,
  productPlanNotifyManagePageProductPlanNotifies,
  productPlanNotifyManagePageQ
} from '../../store/product-plan';

@Component({
  templateUrl: './product-plan-notify-manage-page.component.html',
  styleUrls: ['./product-plan-notify-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPlanNotifyManagePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-product-plan-notify-manage-page') b2 = true;
  readonly displayedColumns = ['type', 'name', 'batch', 'startDate', 'endDate', 'btns'];
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  readonly searchForm: FormGroup = this.fb.group({q: ''});
  readonly productPlanNotifies$: Observable<ProductPlanNotify[]> = this.store.select(productPlanNotifyManagePageProductPlanNotifies);
  readonly count$ = this.store.select(productPlanNotifyManagePageCount);
  readonly pageSize$ = this.store.select(productPlanNotifyManagePagePageSize);
  readonly pageIndex$ = this.store.select(productPlanNotifyManagePagePageIndex);
  readonly q$ = this.store.select(productPlanNotifyManagePageQ);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private translate: TranslateService,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  ngOnInit(): void {
    this.searchForm.get('q').valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(q => {
        this.router.navigate(['productPlan/notifies'], {queryParams: {q}});
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    this.update(ProductPlanNotify.assign());
  }

  update(productPlanNotify: ProductPlanNotify) {
    ProductPlanNotifyUpdateDialogComponent.open(this.dialog, {productPlanNotify})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({productPlanNotify: it}));
        this.utilService.showSuccess();
      });
  }

  delete(productPlanNotify: ProductPlanNotify) {
    const {id} = productPlanNotify;
    // this.utilService.showConfirm().pipe<Action>(
    //   tap(() => this.store.dispatch(new SetLoading())),
    //   switchMap(() => this.apiService.finishProductPlanNotify(id)),
    //   tap(() => this.utilService.showSuccess()),
    //   finalize(() => this.store.dispatch(new SetLoading(false)))
    // ).subscribe(
    //   () => this.store.dispatch(new DeleteSuccess({id})),
    //   err => this.store.dispatch(new ShowError(err))
    // );
  }

  onPageEvent(ev: PageEvent) {
    const {pageIndex, pageSize} = ev;
    const first = pageIndex * pageSize;
    this.q$.pipe(take(1)).subscribe(q => {
      const queryParams = {first, pageSize, q};
      this.router.navigate(['productPlan/notifies'], {queryParams});
    });
  }

}
