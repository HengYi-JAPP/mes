import {CollectionViewer, DataSource, SelectionModel} from '@angular/cdk/collections';
import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {BehaviorSubject, Observable, Subject} from 'rxjs';
import {map, switchMapTo, take, takeUntil} from 'rxjs/operators';
import {LineMachine} from '../../models/line-machine';
import {ProductPlanNotify} from '../../models/product-plan-notify';
import {ApiService} from '../../services/api.service';
import {LineMachineCompare, UtilService} from '../../services/util.service';
import {Exe, ExeBatch, Finish} from '../../store/actions/product-plan-notify-exe-info-page';
import {productPlanNotifyManagePageProductPlanNotify} from '../../store/product-plan';

interface LineMachineExtra extends LineMachine {
  extra?: {
    canExe: boolean,
  };
}

class LineMachineDataSource extends DataSource<LineMachineExtra> {
  readonly selection = new SelectionModel<LineMachineExtra>(true, []);
  private readonly _destroy$ = new Subject();
  private readonly productPlanNotify$ = this.store.select(productPlanNotifyManagePageProductPlanNotify);
  private readonly data$ = new BehaviorSubject<LineMachineExtra[]>([]);

  constructor(private store: Store<any>) {
    super();
    this.productPlanNotify$
      .pipe(
        takeUntil(this._destroy$)
      )
      .subscribe(it => this.refresh(it));
  }

  get isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.canExeData.length;
    return numSelected === numRows;
  }

  get canExeData() {
    return this.data.filter(it => it.extra.canExe);
  }

  private get data() {
    return this.data$.value || [];
  }

  masterToggle() {
    this.isAllSelected ? this.selection.clear() : this.canExeData.forEach(row => this.selection.select(row));
  }

  connect(collectionViewer: CollectionViewer): Observable<LineMachineExtra[]> {
    return this.data$.asObservable();
  }

  disconnect(collectionViewer: CollectionViewer): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private refresh(productPlanNotify: ProductPlanNotify) {
    this.selection.clear();

    function calcCanExe(lineMachine: LineMachine): boolean {
      if (!lineMachine.productPlan) {
        return true;
      }
      const machExeNotify = lineMachine.productPlan.productPlanNotify;
      if (machExeNotify.id === productPlanNotify.id) {
        return false;
      }
      return moment(productPlanNotify.startDate).isAfter(moment(machExeNotify.startDate));
    }

    const next = (productPlanNotify && productPlanNotify.lineMachines || [])
      .map(lineMachine => {
        const canExe = calcCanExe(lineMachine);
        const extra = {canExe};
        return {...lineMachine, extra};
      })
      .sort(LineMachineCompare);
    this.data$.next(next);
  }
}

@Component({
  templateUrl: './product-plan-notify-exe-info-page.component.html',
  styleUrls: ['./product-plan-notify-exe-info-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPlanNotifyExeInfoPageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-product-plan-notify-exe-info-page') b2 = true;
  readonly displayedColumns = ['select', 'workshop', 'line', 'lineMachine', 'exePlan', 'exePlanStartDate', 'btns'];
  readonly productPlanNotify$ = this.store.select(productPlanNotifyManagePageProductPlanNotify);
  readonly dataSource = new LineMachineDataSource(this.store);

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  finish() {
    this.utilService.showConfirm()
      .pipe(
        switchMapTo(this.productPlanNotify$),
        take(1),
        map(it => it.id),
        map(id => new Finish({id}))
      )
      .subscribe(it => this.store.dispatch(it));
  }

  exe(lineMachine: LineMachine) {
    this.productPlanNotify$
      .pipe(
        take(1),
        map(it => it.id),
        map(productPlanNotifyId => new Exe({productPlanNotifyId, lineMachine}))
      )
      .subscribe(it => this.store.dispatch(it));
  }

  exeBatch() {
    this.productPlanNotify$
      .pipe(
        take(1),
        map(it => it.id),
        map(productPlanNotifyId => new ExeBatch({productPlanNotifyId, lineMachines: this.dataSource.selection.selected}))
      )
      .subscribe(it => this.store.dispatch(it));
  }

}
