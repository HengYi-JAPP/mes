import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Actions, Effect} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, finalize, map, switchMap, tap, withLatestFrom} from 'rxjs/operators';
import {LineManagePageComponent} from '../../containers/line-manage-page/line-manage-page.component';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../actions/core';
import {InitSuccess} from '../actions/line-manage-page';
import {lineManagePageWorkshop} from '../config';
import {ofRouteChangePage} from './core.effects';

@Injectable()
export class LineManagePageEffects {
  @Effect() init$ = this.actions$.pipe(
    ofRouteChangePage(LineManagePageComponent),
    tap(() => this.store.dispatch(new SetLoading())),
    withLatestFrom(this.store.select(lineManagePageWorkshop)),
    switchMap(([{payload: {snapshot}}, oldWorkshop]) => {
      let {queryParams: {workshopId}} = snapshot;
      const oldWorkshopId = oldWorkshop && oldWorkshop.id;
      return this.apiService.listWorkshop()
        .pipe(
          switchMap(workshops => {
            workshopId = workshopId || oldWorkshopId || workshops[0].id;
            return this.apiService.getWorkshop_Lines(workshopId)
              .pipe(
                map(lines => new InitSuccess({workshopId, lines, workshops}))
              );
          }),
          catchError(error => of(new ShowError(error))),
          finalize(() => this.store.dispatch(new SetLoading(false)))
        );
    })
  );

  constructor(private actions$: Actions,
              private store: Store<any>,
              private router: Router,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

}
