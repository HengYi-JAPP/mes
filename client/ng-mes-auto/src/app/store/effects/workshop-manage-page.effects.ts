import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {WorkshopManagePageComponent} from '../../containers/workshop-manage-page/workshop-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {Delete, DeleteSuccess, InitSuccess, WorkshopManagePageActionTypes} from '../actions/workshop-manage-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class WorkshopManagePageEffects {
  @Effect()
  init$ = this.actions$.pipe(
    ofRouteChangePage(WorkshopManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(() => {
      return this.apiService.listWorkshop()
        .pipe(
          map(workshops => new InitSuccess({workshops})),
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    }));

  @Effect()
  delete$ = this.actions$.pipe(
    ofType<Delete>(WorkshopManagePageActionTypes.Delete),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(({payload: {id}}) => {
      return this.apiService.deleteWorkshop(id)
        .pipe(
          map(() => new DeleteSuccess({id})),
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
