import {MetaReducer} from '@ngrx/store';

export const environment = {
  production: true
};

export const metaReducers: MetaReducer<any>[] = [];

export const baseApiUrl = `http://${location.host}/api`;
export const baseEventbusUrl = `http://${location.host}/eventbus`;

export const SEARCH_DEBOUNCE_TIME = 300;

/** Http interceptor providers in outside-in order */
export const httpInterceptorProviders = [
  // {provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true}
];
