import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {WorkshopProductPlanReportPageComponent} from '../../containers/workshop-product-plan-report-page/workshop-product-plan-report-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/workshop-product-plan-report-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class WorkshopProductPlanReportPageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(WorkshopProductPlanReportPageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {snapshot}}) => {
      const {queryParams: {workshopId}} = snapshot;
      const params = new HttpParams().set('workshopId', workshopId || '');
      return this.apiService.workshopProductPlanReport(params)
        .pipe(
          map(report => new InitSuccess({report})),
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
