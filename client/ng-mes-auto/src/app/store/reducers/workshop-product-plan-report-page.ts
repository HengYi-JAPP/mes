import {createSelector} from '@ngrx/store';
import {WorkshopProductPlanReport} from '../../models/workshop-product-plan-report';
import {Actions, WorkshopProductPlanReportPageActionTypes} from '../actions/workshop-product-plan-report-page';

export interface State {
  report?: WorkshopProductPlanReport;
}

const initialState: State = {};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case WorkshopProductPlanReportPageActionTypes.InitSuccess: {
      let {report} = action.payload;
      report = WorkshopProductPlanReport.assign(report);
      return {...state, report};
    }

    default:
      return state;
  }
}

export const getReport = (state: State) => state.report;
export const getWorkshop = createSelector(getReport, it => it && it.workshop);
export const getItems = createSelector(getReport, it => it && it.items);
