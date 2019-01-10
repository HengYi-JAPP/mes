import {Action} from '@ngrx/store';
import {Batch} from '../../models/batch';

export enum BatchManagePageActionTypes {
  InitSuccess = '[BatchManagePage] InitSuccess',
  SaveSuccess = '[BatchManagePage] SaveSuccess',
  DeleteSuccess = '[BatchManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = BatchManagePageActionTypes.InitSuccess;

  constructor(public payload: { batches: Batch[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = BatchManagePageActionTypes.SaveSuccess;

  constructor(public payload: { batch: Batch }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = BatchManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}


export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
