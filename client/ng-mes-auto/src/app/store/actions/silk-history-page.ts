import {Action} from '@ngrx/store';
import {SilkCar} from '../../models/silk-car';

export enum SilkHistoryPageActionTypes {
  InitSuccess = '[SilkHistoryPage] InitSuccess',
  SaveSuccess = '[SilkHistoryPage] SaveSuccess',
  DeleteSuccess = '[SilkHistoryPage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = SilkHistoryPageActionTypes.InitSuccess;

  constructor(public payload: { silkCars: SilkCar[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = SilkHistoryPageActionTypes.SaveSuccess;

  constructor(public payload: { silkCar: SilkCar }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = SilkHistoryPageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
