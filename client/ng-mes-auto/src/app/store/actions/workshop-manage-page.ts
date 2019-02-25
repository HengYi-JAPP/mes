import {Action} from '@ngrx/store';
import {Workshop} from '../../models/workshop';

export enum WorkshopManagePageActionTypes {
  InitSuccess = '[WorkshopManagePage] InitSuccess',
  SaveSuccess = '[WorkshopManagePage] SaveSuccess',
  Delete = '[WorkshopManagePage] Delete',
  DeleteSuccess = '[WorkshopManagePage] DeleteSuccess',
  SetQ = '[WorkshopManagePage] SetQ',
}

export class InitSuccess implements Action {
  readonly type = WorkshopManagePageActionTypes.InitSuccess;

  constructor(public payload: { workshops: Workshop[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = WorkshopManagePageActionTypes.SaveSuccess;

  constructor(public payload: { workshop: Workshop }) {
  }
}

export class Delete implements Action {
  readonly type = WorkshopManagePageActionTypes.Delete;

  constructor(public payload: { id: string }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = WorkshopManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export class SetQ implements Action {
  readonly type = WorkshopManagePageActionTypes.SetQ;

  constructor(public payload: { q: string }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess
  | Delete
  | DeleteSuccess
  | SetQ;
