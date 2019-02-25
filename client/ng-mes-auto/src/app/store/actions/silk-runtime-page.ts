import {Action} from '@ngrx/store';
import {SilkCarRuntime} from '../../models/silk-car-runtime';
import {SockjsRec} from './core';

export enum SilkRuntimePageActionTypes {
  FetchSilkCar = '[SilkRuntimePage] FetchSilkCarRuntime',
  FetchSilkCarSuccess = '[SilkRuntimePage] FetchSilkCarRuntimeSuccess',
  SetFireDateTimeSort = '[SilkRuntimePage] SetFireDateTimeSort',
  EventSourceSockjsRec = '[SilkRuntimePage] EventSourceSockjsRec',
}

export class FetchSilkCarRuntime implements Action {
  readonly type = SilkRuntimePageActionTypes.FetchSilkCar;

  constructor(public payload: { code: string }) {
  }
}

export class FetchSilkCarRuntimeSuccess implements Action {
  readonly type = SilkRuntimePageActionTypes.FetchSilkCarSuccess;

  constructor(public payload: { silkCarRuntime: SilkCarRuntime }) {
  }
}

export class SetFireDateTimeSort implements Action {
  readonly type = SilkRuntimePageActionTypes.SetFireDateTimeSort;

  constructor(public payload: { direction: string }) {
  }
}

export class EventSourceSockjsRec implements Action {
  readonly type = SilkRuntimePageActionTypes.EventSourceSockjsRec;

  constructor(public payload: { sockjsRec: SockjsRec }) {
  }
}

export type Actions =
  | FetchSilkCarRuntime
  | EventSourceSockjsRec
  | FetchSilkCarRuntimeSuccess
  | SetFireDateTimeSort;
