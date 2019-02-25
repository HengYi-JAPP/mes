import {Params, RouterStateSnapshot} from '@angular/router';
import {routerReducer, RouterReducerState, RouterStateSerializer} from '@ngrx/router-store';
import {ActionReducerMap, createSelector} from '@ngrx/store';
import * as core from './reducers/core';

export interface RouterStateUrl {
  url: string;
  params: Params;
  queryParams: Params;
}

export class CustomSerializer implements RouterStateSerializer<RouterStateUrl> {
  serialize(routerState: RouterStateSnapshot): RouterStateUrl {
    let route = routerState.root;

    while (route.firstChild) {
      route = route.firstChild;
    }

    const {url, root: {queryParams}} = routerState;
    const {params} = route;

    // Only return an object including the URL, params and query params
    // instead of the entire snapshot
    return {url, params, queryParams};
  }
}

export interface State {
  router: RouterReducerState<RouterStateUrl>;
  core: core.State;
}

export const reducers: ActionReducerMap<State> = {
  router: routerReducer,
  core: core.reducer
};

export const appRouterState = state => state.router;

export const coreState = state => state.core;
export const coreLoading = createSelector(coreState, core.getLoading);
export const coreAuthOperator = createSelector(coreState, core.getAuthOperator);
export const coreAuthAdmin = createSelector(coreState, core.getAuthAdmin);
