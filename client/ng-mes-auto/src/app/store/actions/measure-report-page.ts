import {Action} from '@ngrx/store';
import {MeasureReport} from '../../models/measure-report';

export enum MeasureReportPageActionTypes {
  Search = '[MeasureReportPage] Search',
  SearchSuccess = '[MeasureReportPage] SearchSuccess',
}

export class Search implements Action {
  readonly type = MeasureReportPageActionTypes.Search;

  constructor(public payload: { workshopId: string, date: Date | string, budatClassId: string }) {
  }
}

export class SearchSuccess implements Action {
  readonly type = MeasureReportPageActionTypes.SearchSuccess;

  constructor(public payload: { report: MeasureReport }) {
  }
}


export type Actions =
  | SearchSuccess;
