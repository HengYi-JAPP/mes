import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {ProductManagePageComponent} from '../../containers/product-manage-page/product-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/product-manage-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class ProductManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(ProductManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(() => {
      return this.apiService.listProduct()
        .pipe(
          map(products => new InitSuccess({products})),
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
