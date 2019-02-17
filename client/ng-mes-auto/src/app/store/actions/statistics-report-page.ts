import {Action} from '@ngrx/store';
import {StatisticsReport} from '../../models/statistics-report';

export enum StatisticsReportPageActionTypes {
  Search = '[StatisticsReportPage] Search',
  SearchSuccess = '[StatisticsReportPage] SearchSuccess',
}

export class Search implements Action {
  readonly type = StatisticsReportPageActionTypes.Search;

  constructor(public payload: { workshopId: string, startDate: Date | string, endDate: Date | string }) {
  }
}

export class SearchSuccess implements Action {
  readonly type = StatisticsReportPageActionTypes.SearchSuccess;

  constructor(public payload: { report: StatisticsReport }) {
  }
}


export type Actions =
  | SearchSuccess;
