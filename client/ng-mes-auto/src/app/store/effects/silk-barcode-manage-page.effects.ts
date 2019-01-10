import {HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {SilkBarcodeManagePageComponent} from '../../containers/silk-barcode-manage-page/silk-barcode-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/printer-manage-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class SilkBarcodeManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(SilkBarcodeManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {snapshot}}) => {
      const {queryParams} = snapshot;
      const q = queryParams.q || '';
      const params = new HttpParams();
      return this.apiService.listBatch(params)
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
