import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as batchManagePage from './reducers/batch-manage-page';
import * as productPlanNotifyExeInfoPage from './reducers/product-plan-notify-exe-info-page';
import * as productPlanNotifyManagePage from './reducers/product-plan-notify-manage-page';
import * as workshopProductPlanReportPage from './reducers/workshop-product-plan-report-page';

export interface State {
  productPlanNotifyManagePage: productPlanNotifyManagePage.State;
  productPlanNotifyExeInfoPage: productPlanNotifyExeInfoPage.State;
  batchManagePage: batchManagePage.State;
  workshopProductPlanReportPage: workshopProductPlanReportPage.State;
}

export const reducers = {
  productPlanNotifyManagePage: productPlanNotifyManagePage.reducer,
  productPlanNotifyExeInfoPage: productPlanNotifyExeInfoPage.reducer,
  batchManagePage: batchManagePage.reducer,
  workshopProductPlanReportPage: workshopProductPlanReportPage.reducer
};

export const featureName = 'productPlan';
export const featureState = createFeatureSelector<State>(featureName);

export const productPlanNotifyManagePageState = createSelector(featureState, state => state.productPlanNotifyManagePage);
export const productPlanNotifyManagePageProductPlanNotifies = createSelector(productPlanNotifyManagePageState, productPlanNotifyManagePage.getProductPlanNotifies);
export const productPlanNotifyManagePageCount = createSelector(productPlanNotifyManagePageState, productPlanNotifyManagePage.getCount);
export const productPlanNotifyManagePagePageSize = createSelector(productPlanNotifyManagePageState, productPlanNotifyManagePage.getPageSize);
export const productPlanNotifyManagePageQ = createSelector(productPlanNotifyManagePageState, productPlanNotifyManagePage.getQ);
export const productPlanNotifyManagePagePageIndex = createSelector(productPlanNotifyManagePageState, productPlanNotifyManagePage.getPageIndex);

export const productPlanNotifyExeInfoPageState = createSelector(featureState, state => state.productPlanNotifyExeInfoPage);
export const productPlanNotifyManagePageProductPlanNotify = createSelector(productPlanNotifyExeInfoPageState, productPlanNotifyExeInfoPage.getProductPlanNotify);

export const batchManagePageState = createSelector(featureState, state => state.batchManagePage);
export const batchManagePageBatches = createSelector(batchManagePageState, batchManagePage.getBatches);
export const batchManagePageCount = createSelector(batchManagePageState, batchManagePage.getCount);
export const batchManagePagePageSize = createSelector(batchManagePageState, batchManagePage.getPageSize);
export const batchManagePagePageIndex = createSelector(batchManagePageState, batchManagePage.getPageIndex);
export const batchManagePageQ = createSelector(batchManagePageState, batchManagePage.getQ);

export const workshopProductPlanReportPageState = createSelector(featureState, state => state.workshopProductPlanReportPage);
export const workshopProductPlanReportPageWorkshop = createSelector(workshopProductPlanReportPageState, workshopProductPlanReportPage.getWorkshop);
export const workshopProductPlanReportPageItems = createSelector(workshopProductPlanReportPageState, workshopProductPlanReportPage.getItems);
