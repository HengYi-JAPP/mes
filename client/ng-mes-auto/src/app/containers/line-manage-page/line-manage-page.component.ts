import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog, MatSelectChange} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {Observable, of, Subject} from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  filter,
  finalize,
  map,
  switchMap,
  take,
  takeUntil,
  tap,
  withLatestFrom
} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {LineUpdateDialogComponent} from '../../components/line-update-dialog/line-update-dialog.component';
import {Line} from '../../models/line';
import {Workshop} from '../../models/workshop';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../../store/actions/core';
import {DeleteSuccess, SaveSuccess, SetQ} from '../../store/actions/line-manage-page';
import {lineManagePageLines, lineManagePageWorkshop, lineManagePageWorkshops} from '../../store/config';

@Component({
  templateUrl: './line-manage-page.component.html',
  styleUrls: ['./line-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineManagePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-line-manage-page') b2 = true;
  readonly displayedColumns = ['workshop', 'name', 'doffingType', 'btns'];
  readonly searchForm: FormGroup;
  readonly workshops$: Observable<Workshop[]>;
  readonly workshop$: Observable<Workshop>;
  readonly lines$: Observable<Line[]>;
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.workshops$ = this.store.select(lineManagePageWorkshops);
    this.workshop$ = this.store.select(lineManagePageWorkshop);
    this.lines$ = this.store.select(lineManagePageLines);
    this.searchForm = this.fb.group({
      'q': ''
    });
  }

  get q() {
    return this.searchForm.get('q');
  }

  ngOnInit(): void {
    this.q.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged(),
        map(q => new SetQ({q}))
      )
      .subscribe(it => this.store.dispatch(it));
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  workshopChange(ev: MatSelectChange): void {
    const queryParams = {workshopId: ev.value};
    this.router.navigate(['config/lines'], {queryParams});
  }

  batchCreate() {
    LineUpdateDialogComponent.batchCreate(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => it && (it.length > 0)),
        withLatestFrom(this.workshop$)
      )
      .subscribe(([lines, workshop]) => {
        const workshopId = lines[0].workshop.id;
        if (workshopId === workshop.id) {
          lines.forEach(line => {
            this.store.dispatch(new SaveSuccess({line}));
          });
        } else {
          this.router.navigate(['config', 'lines'], {queryParams: {workshopId}});
        }
        this.utilService.showSuccess();
      });
  }

  create() {
    this.workshop$
      .pipe(
        take(1)
      )
      .subscribe(workshop => {
        this.update(Line.assign({workshop}));
      });
  }

  update(line: Line) {
    LineUpdateDialogComponent.open(this.dialog, {line})
      .afterClosed()
      .pipe(
        filter(it => !!it),
        withLatestFrom(this.workshop$)
      )
      .subscribe(([newLine, workshop]) => {
        const workshopId = newLine.workshop.id;
        if (workshopId === workshop.id) {
          this.store.dispatch(new SaveSuccess({line: newLine}));
        } else {
          this.router.navigate(['config', 'lines'], {queryParams: {workshopId}});
        }
        this.utilService.showSuccess();
      });
  }

  delete(line: Line) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deleteLine(line.id)),
        map(() => new DeleteSuccess({id: line.id})),
        tap(() => this.utilService.showSuccess()),
        catchError(error => of(new ShowError(error))),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(it => this.store.dispatch(it));
  }

}
