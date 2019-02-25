import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Action, Store} from '@ngrx/store';
import {Observable, of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {ProductPlanNotifyExeInfoPageComponent} from '../../containers/product-plan-notify-exe-info-page/product-plan-notify-exe-info-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {RouterGo, SetLoading, ShowError} from '../actions/core';
import {Exe, ExeBatch, Finish, InitSuccess, ProductPlanNotifyExeInfoPageActionTypes} from '../actions/product-plan-notify-exe-info-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class ProductPlanNotifyExeInfoPageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(ProductPlanNotifyExeInfoPageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {snapshot}}) => {
      const {params: {id}} = snapshot;
      return this.init(id)
        .pipe(
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    }));

  @Effect() exe$ = this.actions$.pipe(
    ofType<Exe>(ProductPlanNotifyExeInfoPageActionTypes.Exe),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {productPlanNotifyId, lineMachine}}) => {
      return this.apiService.exeProductPlanNotify(productPlanNotifyId, lineMachine)
        .pipe(
          switchMap(() => this.init(productPlanNotifyId)),
          tap(() => this.utilService.showSuccess()),
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    }));

  @Effect() exeBatch$ = this.actions$.pipe(
    ofType<ExeBatch>(ProductPlanNotifyExeInfoPageActionTypes.ExeBatch),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {productPlanNotifyId, lineMachines}}) => {
      return this.apiService.batchExeProductPlanNotify(productPlanNotifyId, lineMachines)
        .pipe(
          switchMap(() => this.init(productPlanNotifyId)),
          tap(() => this.utilService.showSuccess()),
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    }));

  @Effect() finish$ = this.actions$.pipe(
    ofType<Finish>(ProductPlanNotifyExeInfoPageActionTypes.Finish),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {id}}) => {
      return this.apiService.finishProductPlanNotify(id)
        .pipe(
          map(() => new RouterGo({path: ['productPlan/notifies']})),
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

  private init(id: string): Observable<Action> {
    return this.apiService.getProductPlanNotify_exeInfo(id)
      .pipe(
        map(it => new InitSuccess(it))
      );
  }

}
