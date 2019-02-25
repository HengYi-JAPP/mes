import {Action} from '@ngrx/store';
import {WorkshopProductPlanReport} from '../../models/workshop-product-plan-report';

export enum WorkshopProductPlanReportPageActionTypes {
  InitSuccess = '[WorkshopProductPlanReportPage] InitSuccess',
}

export class InitSuccess implements Action {
  readonly type = WorkshopProductPlanReportPageActionTypes.InitSuccess;

  constructor(public payload: { report: WorkshopProductPlanReport }) {
  }
}


export type Actions =
  | InitSuccess;
