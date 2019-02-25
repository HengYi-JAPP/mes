import {Action} from '@ngrx/store';
import {Product} from '../../models/product';

export enum ProductConfgPageActionTypes {
  InitSuccess = '[ProductConfigPage] InitSuccess',
  SaveSuccess = '[ProductConfigPage] SaveSuccess',
}

export class InitSuccess implements Action {
  readonly type = ProductConfgPageActionTypes.InitSuccess;

  constructor(public payload: { products: Product[], id: string }) {
  }
}

export class SaveSuccess implements Action {
  readonly type = ProductConfgPageActionTypes.SaveSuccess;

  constructor(public payload: { product: Product }) {
  }
}

export type Actions =
  | SaveSuccess
  | InitSuccess;
