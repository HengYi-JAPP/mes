import {Action} from '@ngrx/store';
import {Line} from '../../models/line';
import {LineMachine} from '../../models/line-machine';

export enum LineMachineManagePageActionTypes {
  InitSuccess = '[LineMachineManagePage] InitSuccess',
  SaveSuccess = '[LineMachineManagePage] SaveSuccess',
  DeleteSuccess = '[LineMachineManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = LineMachineManagePageActionTypes.InitSuccess;

  constructor(public payload: { lineId: string, lines: Line[], lineMachines: LineMachine[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = LineMachineManagePageActionTypes.SaveSuccess;

  constructor(public payload: { lineMachine: LineMachine }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = LineMachineManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
