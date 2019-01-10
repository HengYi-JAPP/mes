import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as gradeManagePage from './reducers/grade-manage-page';
import * as lineMachineManagePage from './reducers/line-machine-manage-page';
import * as lineManagePage from './reducers/line-manage-page';
import * as productConfigPage from './reducers/product-config-page';
import * as productManagePage from './reducers/product-manage-page';
import * as silkCarManagePage from './reducers/silk-car-manage-page';
import * as workshopManagePage from './reducers/workshop-manage-page';

export interface State {
  silkCarManagePage: silkCarManagePage.State;
  lineManagePage: lineManagePage.State;
  workshopManagePage: workshopManagePage.State;
  productManagePage: productManagePage.State;
  productConfigPage: productConfigPage.State;
  lineMachineManagePage: lineMachineManagePage.State;
  gradeManagePage: gradeManagePage.State;
}

export const reducers = {
  silkCarManagePage: silkCarManagePage.reducer,
  lineManagePage: lineManagePage.reducer,
  workshopManagePage: workshopManagePage.reducer,
  productManagePage: productManagePage.reducer,
  productConfigPage: productConfigPage.reducer,
  lineMachineManagePage: lineMachineManagePage.reducer,
  gradeManagePage: gradeManagePage.reducer
};

export const featureName = 'configFeature';
export const featureState = createFeatureSelector<State>(featureName);

export const workshopManagePageState = createSelector(featureState, state => state.workshopManagePage);
export const workshopManagePageWorkshops = createSelector(workshopManagePageState, workshopManagePage.getWorkshops);

export const lineManagePageState = createSelector(featureState, state => state.lineManagePage);
export const lineManagePageWorkshops = createSelector(lineManagePageState, lineManagePage.getWorkshops);
export const lineManagePageWorkshop = createSelector(lineManagePageState, lineManagePage.getWorkshop);
export const lineManagePageLines = createSelector(lineManagePageState, lineManagePage.getLines);

export const lineMachineManagePageState = createSelector(featureState, state => state.lineMachineManagePage);
export const lineMachineManagePageLines = createSelector(lineMachineManagePageState, lineMachineManagePage.getLines);
export const lineMachineManagePageLine = createSelector(lineMachineManagePageState, lineMachineManagePage.getLine);
export const lineMachineManagePageLineMachines = createSelector(lineMachineManagePageState, lineMachineManagePage.getLineMachines);

export const productManagePageState = createSelector(featureState, state => state.productManagePage);
export const productManagePageProducts = createSelector(productManagePageState, productManagePage.getProducts);

export const productConfigPageState = createSelector(featureState, state => state.productConfigPage);
export const productConfigPageProducts = createSelector(productConfigPageState, productConfigPage.getProducts);
export const productConfigPageProduct = createSelector(productConfigPageState, productConfigPage.getProduct);

export const silkCarManagePageState = createSelector(featureState, state => state.silkCarManagePage);
export const silkCarManagePageSilkCars = createSelector(silkCarManagePageState, silkCarManagePage.getSilkCars);
export const silkCarManagePageCount = createSelector(silkCarManagePageState, silkCarManagePage.getCount);
export const silkCarManagePagePageSize = createSelector(silkCarManagePageState, silkCarManagePage.getPageSize);
export const silkCarManagePagePageIndex = createSelector(silkCarManagePageState, silkCarManagePage.getPageIndex);
export const silkCarManagePageQ = createSelector(silkCarManagePageState, silkCarManagePage.getQ);

export const gradeManagePageState = createSelector(featureState, state => state.gradeManagePage);
export const gradeManagePageGrades = createSelector(gradeManagePageState, gradeManagePage.getGrades);
