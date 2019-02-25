import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {SilkCarManagePageComponent} from '../../containers/silk-car-manage-page/silk-car-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/silk-car-manage-page';
import {silkCarManagePageState} from '../config';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class SilkCarManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(SilkCarManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(silkCarManagePageState)),
    switchMap(([{payload: {snapshot}}, oldState]) => {
      const {queryParams} = snapshot;
      const q = queryParams.q || '';
      const params = new HttpParams()
        .set('pageSize', queryParams.pageSize || oldState.pageSize || '20')
        .set('first', queryParams.first || '0')
        .set('q', q);
      return this.apiService.listSilkCar(params)
        .pipe(
          map(it => new InitSuccess({...it, q})),
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
