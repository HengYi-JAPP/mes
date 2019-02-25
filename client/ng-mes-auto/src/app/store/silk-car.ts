import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as silkHistoryPage from './reducers/silk-history-page';
import * as silkRuntimePage from './reducers/silk-runtime-page';

export interface State {
  silkRuntimePage: silkRuntimePage.State;
  silkHistoryPage: silkHistoryPage.State;
}

export const reducers = {
  silkRuntimePage: silkRuntimePage.reducer,
  silkHistoryPage: silkHistoryPage.reducer
};

export const featureName = 'silkCarFeature';
export const featureState = createFeatureSelector<State>(featureName);

export const silkRuntimePageState = createSelector(featureState, state => state.silkRuntimePage);
export const silkRuntimePageSilkCarRuntime = createSelector(silkRuntimePageState, silkRuntimePage.getSilkCarRuntime);
export const silkRuntimePageSilkCarRecord = createSelector(silkRuntimePageState, silkRuntimePage.getSilkCarRecord);
export const silkRuntimePageEventSources = createSelector(silkRuntimePageState, silkRuntimePage.getEventSources);
export const silkRuntimePageFireDateTimeSortDirection = createSelector(silkRuntimePageState, silkRuntimePage.getFireDateTimeSortDirection);

export const silkHistoryPageState = createSelector(featureState, state => state.silkHistoryPage);
