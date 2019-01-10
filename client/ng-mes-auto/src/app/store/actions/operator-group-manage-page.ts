import {Action} from '@ngrx/store';
import {OperatorGroup} from '../../models/operator-group';

export enum OperatorGroupManagePageActionTypes {
  InitSuccess = '[OperatorGroupManagePage] InitSuccess',
  SaveSuccess = '[OperatorGroupManagePage] SaveSuccess',
  SetQ = '[OperatorGroupManagePage] SetQ',
}

export class InitSuccess implements Action {
  readonly type = OperatorGroupManagePageActionTypes.InitSuccess;

  constructor(public payload: { operatorGroups: OperatorGroup[], q?: string }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = OperatorGroupManagePageActionTypes.SaveSuccess;

  constructor(public payload: { operatorGroup: OperatorGroup }) {
  }
}

export class SetQ implements Action {
  readonly type = OperatorGroupManagePageActionTypes.SetQ;

  constructor(public payload: { q: string }) {
  }
}

export type Actions =
  | InitSuccess
  | SetQ
  | SaveSuccess;
