import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog, MatPaginator, MatSelectChange} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {workshopProductPlanReportPageItems, workshopProductPlanReportPageWorkshop} from '../../store/product-plan';

@Component({
  templateUrl: './workshop-product-plan-report-page.component.html',
  styleUrls: ['./workshop-product-plan-report-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkshopProductPlanReportPageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-workshop-product-plan-report-page') b2 = true;
  @ViewChild(MatPaginator) paginator: MatPaginator;
  readonly displayedColumns = ['line', 'batchNo', 'batchSpec', 'tubeColor', 'lineMachineSpecs', 'lineMachineCount'];
  readonly workshops$ = this.apiService.listWorkshop();
  readonly workshop$ = this.store.select(workshopProductPlanReportPageWorkshop);
  readonly items$ = this.store.select(workshopProductPlanReportPageItems);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private router: Router,
              private route: ActivatedRoute,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  workshopChange(ev: MatSelectChange): void {
    const queryParams = {workshopId: ev.value};
    this.router.navigate(['productPlan/workshopProductPlanReport'], {queryParams});
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

}
