import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog, MatPaginator, PageEvent} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {Observable, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, take, takeUntil, withLatestFrom} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {OperatorImportDialogComponent} from '../../components/operator-import-dialog/operator-import-dialog.component';
import {OperatorUpdateDialogComponent} from '../../components/operator-update-dialog/operator-update-dialog.component';
import {Operator} from '../../models/operator';
import {ApiService} from '../../services/api.service';
import {PAGE_SIZE_OPTIONS, UtilService} from '../../services/util.service';
import {SaveSuccess} from '../../store/actions/operator-manage-page';
import {
  operatorManagePageCount,
  operatorManagePageOperators,
  operatorManagePagePageIndex,
  operatorManagePagePageSize,
  operatorManagePageQ
} from '../../store/admin';

@Component({
  templateUrl: './operator-manage-page.component.html',
  styleUrls: ['./operator-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperatorManagePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.operator-manage-page') b2 = true;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  readonly displayedColumns = ['name', 'hrId', 'oaId', 'btns'];
  readonly searchForm: FormGroup;
  readonly operators$: Observable<Operator[]>;
  readonly count$: Observable<number>;
  readonly pageSize$: Observable<number>;
  readonly pageIndex$: Observable<number>;
  readonly q$: Observable<string>;
  readonly pageSizeOptions = PAGE_SIZE_OPTIONS;
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.operators$ = this.store.select(operatorManagePageOperators);
    this.count$ = this.store.select(operatorManagePageCount);
    this.pageSize$ = this.store.select(operatorManagePagePageSize);
    this.pageIndex$ = this.store.select(operatorManagePagePageIndex);
    this.q$ = this.store.select(operatorManagePageQ);
    this.searchForm = fb.group({
      'q': ''
    });
  }

  get q() {
    return this.searchForm.get('q');
  }

  ngOnInit() {
    this.q.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged(),
        withLatestFrom(this.pageSize$)
      )
      .subscribe(([q, pageSize]) => {
        const queryParams = {q, pageSize};
        this.router.navigate(['admin', 'operators'], {queryParams});
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    OperatorImportDialogComponent.open(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(operator => {
        this.store.dispatch(new SaveSuccess({operator}));
        this.update(operator);
        this.utilService.showSuccess();
      });
  }

  update(operator: Operator) {
    OperatorUpdateDialogComponent.open(this.dialog, {operator})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({operator: it}));
        this.utilService.showSuccess();
      });
  }

  delete(operator: Operator) {
  }

  onPageEvent(ev: PageEvent) {
    const {pageIndex, pageSize} = ev;
    const first = pageIndex * pageSize;
    this.q$.pipe(take(1))
      .subscribe(q => {
        const queryParams = {first, pageSize, q};
        this.router.navigate(['admin', 'operators'], {queryParams});
      });
  }

}
