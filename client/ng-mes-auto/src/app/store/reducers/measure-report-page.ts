import {createSelector} from '@ngrx/store';
import {MeasureReport} from '../../models/measure-report';
import {Actions, MeasureReportPageActionTypes} from '../actions/measure-report-page';

export interface State {
  report: MeasureReport;
}

const initialState: State = {
  report: null,
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case MeasureReportPageActionTypes.SearchSuccess: {
      const {report} = action.payload;
      return {...state, report};
    }

    default:
      return state;
  }
}

export const getReport = (state: State) => state.report;
export const getReportItems = createSelector(getReport, report => {
  const items = report && report.items || [];
  return items.sort((o1, o2) => {
    const batchNoCompare = o1.batch.batchNo.localeCompare(o2.batch.batchNo);
    if (batchNoCompare !== 0) {
      return batchNoCompare;
    }
    return o2.grade.sortBy - o1.grade.sortBy;
  });
});
export const getWorkshop = createSelector(getReport, report => report && report.workshop);
export const getDate = createSelector(getReport, report => report && report.date);
export const getBudatClass = createSelector(getReport, report => report && report.budatClass);
