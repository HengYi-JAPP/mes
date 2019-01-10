import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {ProductPlanNotifyManagePageComponent} from '../../containers/product-plan-notify-manage-page/product-plan-notify-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/product-plan-notify-manage-page';
import {productPlanNotifyManagePageState} from '../product-plan';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class ProductPlanNotifyHistoryManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(ProductPlanNotifyManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(productPlanNotifyManagePageState)),
    switchMap(([{payload: {snapshot}}, oldState]) => {
      const {queryParams} = snapshot;
      const q = queryParams.q || '';
      const params = new HttpParams()
        .set('history', '1')
        .set('pageSize', queryParams.pageSize || oldState.pageSize || '20')
        .set('first', queryParams.first || '0')
        .set('q', q);
      return this.apiService.listProductPlanNotify(params)
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
