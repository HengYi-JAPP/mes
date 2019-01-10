import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap} from 'rxjs/operators';
import {GradeManagePageComponent} from '../../containers/grade-manage-page/grade-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/grade-manage-page';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class GradeManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(GradeManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    switchMap(() => {
      return this.apiService.listGrade()
        .pipe(
          map(grades => new InitSuccess({grades})),
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
