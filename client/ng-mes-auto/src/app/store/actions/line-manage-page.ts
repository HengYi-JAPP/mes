import {Action} from '@ngrx/store';
import {Line} from '../../models/line';
import {Workshop} from '../../models/workshop';

export enum LineManagePageActionTypes {
  InitSuccess = '[LineManagePage] InitSuccess',
  SaveSuccess = '[LineManagePage] SaveSuccess',
  DeleteSuccess = '[LineManagePage] DeleteSuccess',
  SetQ = '[LineManagePage] SetQ',
}

export class InitSuccess implements Action {
  readonly type = LineManagePageActionTypes.InitSuccess;

  constructor(public payload: { workshopId: string, workshops: Workshop[]; lines: Line[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = LineManagePageActionTypes.SaveSuccess;

  constructor(public payload: { line: Line }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = LineManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export class SetQ implements Action {
  readonly type = LineManagePageActionTypes.SetQ;

  constructor(public payload: { q: string }) {
  }
}

export type Actions =
  | InitSuccess
  | SaveSuccess
  | DeleteSuccess
  | SetQ;
