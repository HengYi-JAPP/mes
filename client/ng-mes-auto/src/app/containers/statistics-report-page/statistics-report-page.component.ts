import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Router} from '@angular/router';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {StatisticsReportExportDialogComponent} from '../../components/statistics-report-export-dialog/statistics-report-export-dialog.component';
import {Line} from '../../models/line';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {Search} from '../../store/actions/statistics-report-page';
import {
  statisticsReportPageStateEndDate,
  statisticsReportPageStateReport,
  statisticsReportPageStateReportItems,
  statisticsReportPageStateStartDate,
  statisticsReportPageStateWorkshop
} from '../../store/report';

@Component({
  templateUrl: './statistics-report-page.component.html',
  styleUrls: ['./statistics-report-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatisticsReportPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.statistics-report-page') readonly b2 = true;
  readonly displayedColumns = ['line', 'batch', 'grade', 'packageBoxCount', 'silkCount', 'weightCount', 'foamCount'];
  readonly searchForm: FormGroup;
  readonly workshops$ = this.apiService.listWorkshop();
  readonly report$ = this.store.select(statisticsReportPageStateReport);
  readonly reportItems$ = this.store.select(statisticsReportPageStateReportItems);
  readonly workshop$ = this.store.select(statisticsReportPageStateWorkshop);
  readonly startDate$ = this.store.select(statisticsReportPageStateStartDate);
  readonly endDate$ = this.store.select(statisticsReportPageStateEndDate);

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.searchForm = this.fb.group({
      'workshopId': null,
      'startDate': moment(),
      'endDate': moment(),
    });
    this.workshop$.subscribe(it => this.searchForm.patchValue({'workshopId': it && it.id}));
    this.startDate$.subscribe(it => this.searchForm.patchValue({'startDate': moment(it)}));
    this.endDate$.subscribe(it => this.searchForm.patchValue({'endDate': moment(it)}));
  }

  search() {
    const queryParams = this.searchForm.value;
    this.store.dispatch(new Search(queryParams));
  }

  exportDay() {
    StatisticsReportExportDialogComponent.openExportDay(this.dialog, {});
  }

  exportWeek() {
    StatisticsReportExportDialogComponent.openExportWeek(this.dialog, {});
  }

  exportMonth() {
    StatisticsReportExportDialogComponent.openExportMonth(this.dialog, {});
  }

  exportYear() {
    StatisticsReportExportDialogComponent.openExportYear(this.dialog, {});
  }

  detail(line: Line) {
  }
}
