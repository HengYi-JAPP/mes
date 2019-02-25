import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as measureReportPage from './reducers/measure-report-page';
import * as statisticsReportPage from './reducers/statistics-report-page';

export interface State {
  measureReportPage: measureReportPage.State;
  statisticsReportPage: statisticsReportPage.State;
}

export const reducers = {
  measureReportPage: measureReportPage.reducer,
  statisticsReportPage: statisticsReportPage.reducer,
};

export const featureName = 'reportFeature';
export const featureState = createFeatureSelector<State>(featureName);

export const measureReportPageState = createSelector(featureState, state => state.measureReportPage);
export const measureReportPageStateReport = createSelector(measureReportPageState, measureReportPage.getReport);
export const measureReportPageStateReportItems = createSelector(measureReportPageState, measureReportPage.getReportItems);
export const measureReportPageStateWorkshop = createSelector(measureReportPageState, measureReportPage.getWorkshop);
export const measureReportPageStateDate = createSelector(measureReportPageState, measureReportPage.getDate);
export const measureReportPageStateBudatClass = createSelector(measureReportPageState, measureReportPage.getBudatClass);

export const statisticsReportPageState = createSelector(featureState, state => state.statisticsReportPage);
export const statisticsReportPageStateReport = createSelector(statisticsReportPageState, statisticsReportPage.getReport);
export const statisticsReportPageStateReportItems = createSelector(statisticsReportPageState, statisticsReportPage.getReportItems);
export const statisticsReportPageStateWorkshop = createSelector(statisticsReportPageState, statisticsReportPage.getWorkshop);
export const statisticsReportPageStateStartDate = createSelector(statisticsReportPageState, statisticsReportPage.getStartDate);
export const statisticsReportPageStateEndDate = createSelector(statisticsReportPageState, statisticsReportPage.getEndDate);
