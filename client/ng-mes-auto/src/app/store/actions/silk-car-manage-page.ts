import {Action} from '@ngrx/store';
import {SilkCar} from '../../models/silk-car';

export enum SilkCarManagePageActionTypes {
  InitSuccess = '[SilkCarManagePage] InitSuccess',
  SaveSuccess = '[SilkCarManagePage] SaveSuccess',
  DeleteSuccess = '[SilkCarManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = SilkCarManagePageActionTypes.InitSuccess;

  constructor(public payload: { silkCars: SilkCar[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = SilkCarManagePageActionTypes.SaveSuccess;

  constructor(public payload: { silkCar: SilkCar }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = SilkCarManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
