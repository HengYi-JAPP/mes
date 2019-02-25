import {Location} from '@angular/common';
import {Injectable, Type} from '@angular/core';
import {ActivationEnd, Router} from '@angular/router';
import {Actions, Effect, ofType} from '@ngrx/effects';
import {Action, Store} from '@ngrx/store';
import {of, OperatorFunction} from 'rxjs';
import {catchError, filter, map, switchMap, tap} from 'rxjs/operators';
import {baseEventbusUrl} from '../../../environments/environment';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {
  CoreActionTypes,
  FetchAuthInfo,
  FetchAuthInfoSuccess,
  RouteChange,
  RouterBack,
  RouterForward,
  RouterGo,
  ShowError,
  SockjsRec
} from '../actions/core';

declare const EventBus: any;

class GlobalMessage {
  type: string;
  address: string;
  body: { type: string; data: any };
}

export function ofRouteChangePage(page: Type<any>): OperatorFunction<Action, RouteChange> {
  return filter<RouteChange>((action: Action) => {
    if (action.type !== CoreActionTypes.RouteChange) {
      return false;
    }
    const {snapshot: {component}} = (action as RouteChange).payload;
    return component === page;
  });
}

@Injectable()
export class CoreEffects {
  @Effect()
  authInfo$ = this.actions$.pipe(
    ofType<FetchAuthInfo>(CoreActionTypes.FetchAuthInfo),
    switchMap(() => this.apiService.authInfo().pipe(
      map(authInfo => new FetchAuthInfoSuccess({authInfo})),
      catchError(error => of(new ShowError(error)))
    ))
  );

  @Effect({dispatch: false})
  showError$ = this.actions$.pipe(
    ofType<ShowError>(CoreActionTypes.ShowError),
    map(it => it.payload),
    tap(it => this.utilService.showError(it))
  );

  @Effect({dispatch: false})
  navigate$ = this.actions$.pipe(
    ofType<RouterGo>(CoreActionTypes.RouterGo),
    map(it => it.payload),
    tap(({path, queryParams, extras}) => this.router.navigate(path, {...extras, queryParams}))
  );

  @Effect({dispatch: false})
  navigateBack$ = this.actions$.pipe(
    ofType<RouterBack>(CoreActionTypes.RouterBack),
    tap(() => this.location.back())
  );

  @Effect({dispatch: false})
  navigateForward$ = this.actions$.pipe(
    ofType<RouterForward>(CoreActionTypes.RouterForward),
    tap(() => this.location.forward())
  );

  constructor(private actions$: Actions,
              private store: Store<any>,
              private router: Router,
              private location: Location,
              private utilService: UtilService,
              private apiService: ApiService) {
    this.listenToRouter();
    this.setUpEventBus();
  }

  private listenToRouter() {
    this.router.events.pipe(
      filter(event => event instanceof ActivationEnd)
    ).subscribe((event: ActivationEnd) => {
      const {snapshot} = event;
      this.store.dispatch(new RouteChange({snapshot}));
    });
  }

  private setUpEventBus() {
    const eventBus = new EventBus(baseEventbusUrl);
    eventBus.enableReconnect(true);
    eventBus.onopen = () => {
      eventBus.registerHandler('sockjs.global', (error, message: { type: string; address: string, body: any }) => {
        console.log('webSocket receive', message);
        message.body = JSON.parse(message.body);
        this.store.dispatch(new SockjsRec(message));
      });
    };
  }

}

// export function ofRoute(route: string | string[]): OperatorFunction<Action, Action> {
//   return filter((action: Action) => {
//     const isRouteAction = action.type === CoreActionTypes.RouteChange;
//     if (isRouteAction) {
//       const routeAction = action as RouteChange;
//       const {snapshot: {routeConfig: {path}}} = routeAction.payload;
//       const routePath = path;
//       if (isArray(route)) {
//         return route.includes(routePath);
//       } else {
//         return routePath === route;
//       }
//     }
//     return isRouteAction;
//   });
// }
