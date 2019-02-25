import {createFeatureSelector, createSelector} from '@ngrx/store';
import * as operatorGroupManagePage from './reducers/operator-group-manage-page';
import * as operatorManagePage from './reducers/operator-manage-page';
import * as permissionManagePage from './reducers/permission-manage-page';

export interface AdminState {
  operatorManagePage: operatorManagePage.State;
  operatorGroupManagePage: operatorGroupManagePage.State;
  permissionManagePage: permissionManagePage.State;
}

export const reducers = {
  operatorManagePage: operatorManagePage.reducer,
  operatorGroupManagePage: operatorGroupManagePage.reducer,
  permissionManagePage: permissionManagePage.reducer
};

export const featureName = 'adminFeature';
export const featureState = createFeatureSelector<AdminState>(featureName);
export const operatorManagePageState = createSelector(featureState, state => state.operatorManagePage);
export const operatorGroupManagePageState = createSelector(featureState, state => state.operatorGroupManagePage);

export const operatorManagePageOperators = createSelector(operatorManagePageState, operatorManagePage.getOperators);
export const operatorManagePageCount = createSelector(operatorManagePageState, operatorManagePage.getCount);
export const operatorManagePagePageSize = createSelector(operatorManagePageState, operatorManagePage.getPageSize);
export const operatorManagePagePageIndex = createSelector(operatorManagePageState, operatorManagePage.getPageIndex);
export const operatorManagePageQ = createSelector(operatorManagePageState, operatorManagePage.getQ);

export const operatorGroupManagePageOperatorGroups = createSelector(operatorGroupManagePageState, operatorGroupManagePage.getOperatorGroups);

export const permissionManagePageState = createSelector(featureState, state => state.permissionManagePage);
export const permissionManagePagePermissions = createSelector(permissionManagePageState, permissionManagePage.getPermissions);
