import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {OperatorGroupManagePageComponent} from '../../containers/operator-group-manage-page/operator-group-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/operator-group-manage-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class OperatorGroupManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(OperatorGroupManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(() => {
      return this.apiService.listOperatorGroup()
        .pipe(
          map(operatorGroups => new InitSuccess({operatorGroups})),
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
