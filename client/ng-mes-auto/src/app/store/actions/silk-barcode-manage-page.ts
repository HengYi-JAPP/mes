import {Action} from '@ngrx/store';
import {Batch} from '../../models/batch';

export enum SilkBarcodeManagePageActionTypes {
  InitSuccess = '[SilkBarcodeManagePage] InitSuccess',
  SaveSuccess = '[SilkBarcodeManagePage] SaveSuccess',
  DeleteSuccess = '[SilkBarcodeManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = SilkBarcodeManagePageActionTypes.InitSuccess;

  constructor(public payload: { batches: Batch[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = SilkBarcodeManagePageActionTypes.SaveSuccess;

  constructor(public payload: { batch: Batch }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = SilkBarcodeManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}


export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
