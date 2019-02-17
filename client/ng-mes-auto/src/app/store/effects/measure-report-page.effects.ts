import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import * as moment from 'moment';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {MeasureReportPageComponent} from '../../containers/measure-report-page/measure-report-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {MeasureReportPageActionTypes, Search, SearchSuccess} from '../actions/measure-report-page';
import {statisticsReportPageState} from '../report';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class MeasureReportPageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(MeasureReportPageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {snapshot}}) => {
      const {queryParams: {workshopId, date, budatClassId}} = snapshot;
      return of(new Search({workshopId, date, budatClassId}));
    }));

  @Effect() search$ = this.actions$.pipe(
    ofType<Search>(MeasureReportPageActionTypes.Search),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(statisticsReportPageState)),
    switchMap(([{payload: {workshopId, date, budatClassId}}, oldState]) => {
      const params = new HttpParams()
        .set('workshopId', workshopId || '')
        .set('budatClassId', budatClassId || '')
        .set('date', moment(date).format('YYYY-MM-DD'));
      return this.apiService.measureReport(params)
        .pipe(
          map(report => new SearchSuccess({report})),
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    }));

  constructor(private actions$: Actions,
              private store: Store<any>,
              private router: Router,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

}
