import {Action} from '@ngrx/store';
import {Grade} from '../../models/grade';

export enum GradeManagePageActionTypes {
  InitSuccess = '[GradeManagePage] InitSuccess',
  SaveSuccess = '[GradeManagePage] SaveSuccess',
  DeleteSuccess = '[GradeManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = GradeManagePageActionTypes.InitSuccess;

  constructor(public payload: { grades: Grade[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = GradeManagePageActionTypes.SaveSuccess;

  constructor(public payload: { grade: Grade }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = GradeManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}


export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
