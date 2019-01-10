import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {SilkCarRuntimePageComponent} from '../../containers/silk-car-runtime-page/silk-car-runtime-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {FetchSilkCarRuntime, FetchSilkCarRuntimeSuccess, SilkRuntimePageActionTypes} from '../actions/silk-runtime-page';
import {silkRuntimePageSilkCarRecord} from '../silk-car';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class SilkRuntimePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(SilkCarRuntimePageComponent),
    map(({payload: {snapshot}}) => {
      const {queryParams: {code}} = snapshot;
      if (!code) {
        return new FetchSilkCarRuntimeSuccess({silkCarRuntime: null});
      }
      return new FetchSilkCarRuntime({code});
    }));

  @Effect() fetchSilkCar$ = this.actions$.pipe(
    ofType<FetchSilkCarRuntime>(SilkRuntimePageActionTypes.FetchSilkCar),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(silkRuntimePageSilkCarRecord)),
    switchMap(([action, silkCarRecord]) => {
      const {code} = action.payload;
      const oldCode = silkCarRecord && silkCarRecord.silkCar && silkCarRecord.silkCar.code;
      if (code === oldCode) {
        this.store.dispatch(new SetLoading(false));
        return of();
      }

      return this.apiService.getSilkCarRuntimeByCode(code)
        .pipe(
          map(silkCarRuntime => new FetchSilkCarRuntimeSuccess({silkCarRuntime})),
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
