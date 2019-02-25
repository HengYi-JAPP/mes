import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';

@Component({
  templateUrl: './statistics-report-export-dialog.component.html',
  styleUrls: ['./statistics-report-export-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatisticsReportExportDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.statistics-report-export-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly mode: string;
  readonly workshops$ = this.apiService.listWorkshop();
  readonly form: FormGroup;
  readonly dateFilter: any;
  readonly exportFormDay: FormGroup;
  readonly exportFormWeek: FormGroup;
  readonly exportFormMonth: FormGroup;
  readonly exportFormYear: FormGroup;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<StatisticsReportExportDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { mode: string }) {
    this.mode = data.mode;
    this.dialogTitle = 'StatisticsReport.' + this.mode;
    this.form = this.fb.group({
      'workshopId': [null, Validators.required],
      'date': [moment(), Validators.required],
      'year': [moment().year(), Validators.required],
    });
    switch (this.mode) {
      case 'exportDay': {
        this.form.get('year').disable();
        break;
      }
      case 'exportWeek': {
        this.form.get('year').disable();
        break;
      }
      case 'exportMonth': {
        this.form.get('year').disable();
        break;
      }
      case 'exportYear': {
        this.form.get('date').disable();
        break;
      }
    }
    this.exportFormDay = this.fb.group({
      'workshopId': null,
      'date': moment(),
    });
    this.exportFormWeek = this.fb.group({
      'workshopId': null,
      'startDate': moment(),
      'endDate': moment(),
    });
    this.exportFormMonth = this.fb.group({});
    this.exportFormYear = this.fb.group({});
  }

  static openExportDay(dialog: MatDialog, data: { workshopId?: string, date?: Date | string }): MatDialogRef<StatisticsReportExportDialogComponent, void> {
    return dialog.open(StatisticsReportExportDialogComponent, {
      panelClass: 'my-dialog',
      data: {...data, mode: 'exportDay'}
    });
  }

  static openExportWeek(dialog: MatDialog, data: { workshopId?: string, date?: Date | string }): MatDialogRef<StatisticsReportExportDialogComponent, void> {
    return dialog.open(StatisticsReportExportDialogComponent, {
      panelClass: 'my-dialog',
      data: {...data, mode: 'exportWeek'}
    });
  }

  static openExportMonth(dialog: MatDialog, data: { workshopId?: string, date?: Date | string }): MatDialogRef<StatisticsReportExportDialogComponent, void> {
    return dialog.open(StatisticsReportExportDialogComponent, {
      panelClass: 'my-dialog',
      data: {...data, mode: 'exportMonth'}
    });
  }

  static openExportYear(dialog: MatDialog, data: { workshopId?: string, date?: Date | string }): MatDialogRef<StatisticsReportExportDialogComponent, void> {
    return dialog.open(StatisticsReportExportDialogComponent, {
      panelClass: 'my-dialog',
      data: {...data, mode: 'exportYear'}
    });
  }

  mondayFilter(d: Date): boolean {
    return moment(d).weekday() === 0;
  }

  exportDay() {
  }

  exportWeek() {
  }
}


@NgModule({
  imports: [
    SharedModule,
  ],
  declarations: [
    StatisticsReportExportDialogComponent
  ],
  entryComponents: [
    StatisticsReportExportDialogComponent
  ],
  exports: [
    StatisticsReportExportDialogComponent
  ]
})
export class StatisticsReportExportDialogComponentModule {
}
