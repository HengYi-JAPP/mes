import {Action} from '@ngrx/store';
import {Product} from '../../models/product';

export enum ProductManagePageActionTypes {
  InitSuccess = '[ProductManagePage] InitSuccess',
  SaveSuccess = '[ProductManagePage] SaveSuccess',
}

export class InitSuccess implements Action {
  readonly type = ProductManagePageActionTypes.InitSuccess;

  constructor(public payload: { products: Product[] }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = ProductManagePageActionTypes.SaveSuccess;

  constructor(public payload: { product: Product }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess;
