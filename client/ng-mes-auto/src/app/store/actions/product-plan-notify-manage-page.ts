import {Action} from '@ngrx/store';
import {ProductPlanNotify} from '../../models/product-plan-notify';

export enum ProductPlanNotifyManagePageActionTypes {
  InitSuccess = '[ProductPlanNotifyManagePage] InitSuccess',
  SaveSuccess = '[ProductPlanNotifyManagePage] SaveSuccess',
  DeleteSuccess = '[ProductPlanNotifyManagePage] DeleteSuccess',
}

export class InitSuccess implements Action {
  readonly type = ProductPlanNotifyManagePageActionTypes.InitSuccess;

  constructor(public payload: { count: number, first: number, pageSize: number, q: string, productPlanNotifies: ProductPlanNotify[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = ProductPlanNotifyManagePageActionTypes.SaveSuccess;

  constructor(public payload: { productPlanNotify: ProductPlanNotify }) {
  }
}

export class DeleteSuccess implements Action {
  readonly type = ProductPlanNotifyManagePageActionTypes.DeleteSuccess;

  constructor(public payload: { id: string }) {
  }
}

export type Actions =
  | SaveSuccess
  | DeleteSuccess
  | InitSuccess;
