import {Action} from '@ngrx/store';
import {Permission} from '../../models/permission';

export enum PermissionManagePageActionTypes {
  InitSuccess = '[PermissionManagePage] InitSuccess',
  SaveSuccess = '[PermissionManagePage] SaveSuccess',
  DeleteSuccess = '[PermissionManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = PermissionManagePageActionTypes.InitSuccess;

  constructor(public payload: { permissions: Permission[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = PermissionManagePageActionTypes.SaveSuccess;

  constructor(public payload: { permission: Permission }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = PermissionManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}


export type Actions =
  | SaveSuccess
  | InitSuccess
  | DeleteSuccess;
