import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {Line} from '../../models/line';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {Search} from '../../store/actions/measure-report-page';
import {
  measureReportPageStateBudatClass,
  measureReportPageStateDate,
  measureReportPageStateReport,
  measureReportPageStateReportItems,
  measureReportPageStateWorkshop
} from '../../store/report';

@Component({
  templateUrl: './measure-report-page.component.html',
  styleUrls: ['./measure-report-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class MeasureReportPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.measure-report-page') readonly b2 = true;
  readonly displayedColumns = ['batch', 'grade', 'packageBoxCount', 'silkCount', 'weightCount', 'foamCount'];
  readonly testColumns = ['sum', 'test1', 'test2'];
  readonly searchForm: FormGroup;
  readonly workshops$ = this.apiService.listWorkshop();
  readonly budatClasses$ = this.apiService.listPackageClass();
  readonly report$ = this.store.select(measureReportPageStateReport);
  readonly reportItems$ = this.store.select(measureReportPageStateReportItems);
  readonly workshop$ = this.store.select(measureReportPageStateWorkshop);
  readonly date$ = this.store.select(measureReportPageStateDate);
  readonly budatClass$ = this.store.select(measureReportPageStateBudatClass);

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.searchForm = this.fb.group({
      'workshopId': null,
      'date': moment([2019, 0, 14]),
      'budatClassId': null,
    });
    this.workshop$.subscribe(it => this.searchForm.patchValue({'workshopId': it && it.id}));
    this.budatClass$.subscribe(it => this.searchForm.patchValue({'budatClassId': it && it.id}));
    this.date$.subscribe(it => this.searchForm.patchValue({'date': moment(it)}));
  }

  search() {
    const queryParams = this.searchForm.value;
    this.store.dispatch(new Search(queryParams));
  }

  export() {
  }

  detail(line: Line) {
  }
}
