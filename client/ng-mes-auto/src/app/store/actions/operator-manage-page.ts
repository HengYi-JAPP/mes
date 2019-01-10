import {Action} from '@ngrx/store';
import {Operator} from '../../models/operator';

export enum OperatorManageActionTypes {
  InitSuccess = '[OperatorManage] InitSuccess',
  SaveSuccess = '[OperatorManage] SaveSuccess',
}

export class InitSuccess implements Action {
  readonly type = OperatorManageActionTypes.InitSuccess;

  constructor(public payload: { operators: Operator[], count: number, q: string, pageSize: number, first: number }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = OperatorManageActionTypes.SaveSuccess;

  constructor(public payload: { operator: Operator }) {
  }
}

export type Actions =
  | InitSuccess
  | SaveSuccess;
