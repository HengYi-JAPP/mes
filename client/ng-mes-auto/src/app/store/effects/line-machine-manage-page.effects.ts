import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {LineMachineManagePageComponent} from '../../containers/line-machine-manage-page/line-machine-manage-page.component';
import {ApiService} from '../../services/api.service';
import {LineCompare, UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/line-machine-manage-page';
import {lineMachineManagePageLine} from '../config';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class LineMachineManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(LineMachineManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(lineMachineManagePageLine)),
    switchMap(([{payload: {snapshot}}, oldLine]) => {
      let {queryParams: {lineId}} = snapshot;
      const oldLineId = oldLine && oldLine.id;
      return this.apiService.listLine()
        .pipe(
          map(({lines}) => lines && lines.sort(LineCompare)),
          switchMap(lines => {
            lineId = lineId || oldLineId || lines[0].id;
            return this.apiService.getLine_LineMachines(lineId)
              .pipe(
                map(lineMachines => new InitSuccess({lineId, lines, lineMachines}))
              );
          }),
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
