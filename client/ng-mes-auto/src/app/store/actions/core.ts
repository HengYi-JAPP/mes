import {NavigationExtras} from '@angular/router';
import {ActivatedRouteSnapshot} from '@angular/router/src/router_state';
import {Action} from '@ngrx/store';
import {AuthInfo} from '../../models/auth-info';

export enum CoreActionTypes {
  FetchAuthInfo = '[Core] FetchAuthInfo',
  FetchAuthInfoSuccess = '[Core] FetchAuthInfoSuccess',
  SetLoading = '[Core] SetLoading',
  ShowError = '[Core] ShowError',
  RouterGo = '[Core] RouterGo',
  RouterBack = '[Core] RouterBack',
  RouterForward = '[Core] RouterForward',
  RouteChange = '[Core] RouteChange',
  SockjsRec = '[Core] SockjsReceive',
}

export class FetchAuthInfo implements Action {
  readonly type = CoreActionTypes.FetchAuthInfo;
}

export class FetchAuthInfoSuccess implements Action {
  readonly type = CoreActionTypes.FetchAuthInfoSuccess;

  constructor(public payload: { authInfo: AuthInfo }) {
  }
}

export class SetLoading implements Action {
  readonly type = CoreActionTypes.SetLoading;

  constructor(public payload = true) {
  }
}

export class ShowError implements Action {
  readonly type = CoreActionTypes.ShowError;

  constructor(public payload: any) {
  }
}

export class RouterGo implements Action {
  readonly type = CoreActionTypes.RouterGo;

  constructor(public payload: { path: any[]; queryParams?: object; extras?: NavigationExtras; }) {
  }
}

export class RouterBack implements Action {
  readonly type = CoreActionTypes.RouterBack;
}

export class RouterForward implements Action {
  readonly type = CoreActionTypes.RouterForward;
}

export class RouteChange implements Action {
  readonly type = CoreActionTypes.RouteChange;

  constructor(public payload: { snapshot: ActivatedRouteSnapshot }) {
  }
}

export class SockjsRec implements Action {
  readonly type = CoreActionTypes.SockjsRec;

  constructor(public payload: { type: string; address: string, body: any }) {
  }
}

export type Actions = SetLoading
  | ShowError
  | FetchAuthInfo
  | FetchAuthInfoSuccess
  | RouterGo
  | RouterBack
  | RouterForward
  | RouteChange
  | SockjsRec;
