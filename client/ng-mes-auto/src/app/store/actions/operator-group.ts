import {Action} from '@ngrx/store';
import {OperatorGroup} from '../../models/operator-group';

export enum OperatorGroupActionTypes {
  SaveSuccess = '[OperatorGroup] SaveSuccess',
}

export class SaveSuccess implements Action {
  readonly type = OperatorGroupActionTypes.SaveSuccess;

  constructor(public payload: { operatorGroup: OperatorGroup }) {
  }
}

export type Actions =
  | SaveSuccess;
