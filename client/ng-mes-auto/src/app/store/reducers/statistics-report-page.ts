import {createSelector} from '@ngrx/store';
import {StatisticsReport} from '../../models/statistics-report';
import {Actions, StatisticsReportPageActionTypes} from '../actions/statistics-report-page';

export interface State {
  report: StatisticsReport;
}

const initialState: State = {
  report: null,
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case StatisticsReportPageActionTypes.SearchSuccess: {
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
    const lineCompare = o1.line.name.localeCompare(o2.line.name);
    if (lineCompare !== 0) {
      return lineCompare;
    }
    const batchNoCompare = o1.batch.batchNo.localeCompare(o2.batch.batchNo);
    if (batchNoCompare !== 0) {
      return batchNoCompare;
    }
    return o2.grade.sortBy - o1.grade.sortBy;
  });
});
export const getWorkshop = createSelector(getReport, report => report && report.workshop);
export const getStartDate = createSelector(getReport, report => report && report.startDate);
export const getEndDate = createSelector(getReport, report => report && report.endDate);
