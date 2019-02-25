import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {StatisticsReportPageComponent} from '../../containers/statistics-report-page/statistics-report-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {Search, SearchSuccess, StatisticsReportPageActionTypes} from '../actions/statistics-report-page';
import {statisticsReportPageState} from '../report';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class StatisticsReportPageEffects {
  // @Effect() init$ = this.actions$.pipe(
  //   ofRouteChangePage(StatisticsReportPageComponent),
  //   tap(() => this.store.dispatch(new SetLoading())),
  //   switchMap(({payload: {snapshot}}) => {
  //     const {queryParams: {workshopId, startDate, endDate}} = snapshot;
  //     return of(new Search({workshopId, startDate, endDate}));
  //   }));
  //
  // @Effect() search$ = this.actions$.pipe(
  //   ofType<Search>(StatisticsReportPageActionTypes.Search),
  //   tap(() => this.store.dispatch(new SetLoading())),
  //   withLatestFrom(this.store.select(statisticsReportPageState)),
  //   switchMap(([{payload: {workshopId, startDate, endDate}}, oldState]) => {
  //     const params = new HttpParams()
  //       .set('workshopId', workshopId || '')
  //       .set('startDate', moment(startDate).format('YYYY-MM-DD'))
  //       .set('endDate', moment(endDate).format('YYYY-MM-DD'));
  //     return this.apiService.statisticsReport(params)
  //       .pipe(
  //         map(report => new SearchSuccess({report})),
  //         catchError(error => of(new ShowError(error))),
  //         finalize(() => this.store.dispatch(new SetLoading(false)))
  //       );
  //   }));

  constructor(private actions$: Actions,
              private store: Store<any>,
              private router: Router,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

}
