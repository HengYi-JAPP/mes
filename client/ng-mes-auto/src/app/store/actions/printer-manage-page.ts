import {Action} from '@ngrx/store';
import {Batch} from '../../models/batch';

export enum PrinterManagePageActionTypes {
  InitSuccess = '[PrinterManagePage] InitSuccess',
  SaveSuccess = '[PrinterManagePage] SaveSuccess',
  DeleteSuccess = '[PrinterManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = PrinterManagePageActionTypes.InitSuccess;

  constructor(public payload: { batches: Batch[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = PrinterManagePageActionTypes.SaveSuccess;

  constructor(public payload: { batch: Batch }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = PrinterManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}


export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
