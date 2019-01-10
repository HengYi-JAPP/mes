import {Action} from '@ngrx/store';
import {LineMachine} from '../../models/line-machine';
import {LineMachineProductPlan} from '../../models/line-machine-product-plan';
import {ProductPlanNotify} from '../../models/product-plan-notify';

export enum ProductPlanNotifyExeInfoPageActionTypes {
  InitSuccess = '[ProductPlanNotifyExeInfoPage] InitSuccess',
  Exe = '[ProductPlanNotifyExeInfoPage] Exe',
  ExeBatch = '[ProductPlanNotifyExeInfoPage] ExeBatch',
  Finish = '[ProductPlanNotifyExeInfoPage] Finish',
}

export class InitSuccess implements Action {
  readonly type = ProductPlanNotifyExeInfoPageActionTypes.InitSuccess;

  constructor(public payload: { productPlanNotify: ProductPlanNotify, lineMachineProductPlans: LineMachineProductPlan[] }) {
  }
}

export class Exe implements Action {
  readonly type = ProductPlanNotifyExeInfoPageActionTypes.Exe;

  constructor(public payload: { productPlanNotifyId: string, lineMachine: LineMachine }) {
  }
}

export class ExeBatch implements Action {
  readonly type = ProductPlanNotifyExeInfoPageActionTypes.ExeBatch;

  constructor(public payload: { productPlanNotifyId: string, lineMachines: LineMachine[] }) {
  }
}

export class Finish implements Action {
  readonly type = ProductPlanNotifyExeInfoPageActionTypes.Finish;

  constructor(public payload: { id: string }) {
  }
}

export type Actions =
  | ExeBatch
  | Exe
  | Finish
  | InitSuccess;
