// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --prod` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.
import {HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ActionReducer, MetaReducer} from '@ngrx/store';

import {Observable} from 'rxjs';

export const environment = {
  production: false
};

export function logger(reducer: ActionReducer<any>): ActionReducer<any> {
  return function (state: any, action: any): any {
    console.log('state', state);
    console.log('action', action);
    return reducer(state, action);
  };
}

export const metaReducers: MetaReducer<any>[] = [logger];

// export const baseApiUrl =  '192.168.1.45:8080/webApi';
// export const baseApiUrl = location.host + '/webApi';
export const baseApiUrl = 'http://localhost:9998/api';
// export const baseApiUrl = 'http://192.168.1.108:9999/api';
export const baseEventbusUrl = 'http://localhost:8080/eventbus';

export const SEARCH_DEBOUNCE_TIME = 500;


/** Pass untouched request through to the next request handler. */
@Injectable({
  providedIn: 'root'
})
export class TokenInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = 'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1aWQiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUiLCJpYXQiOjE1NDcwNDU2NTQsImlzcyI6ImphcHAtbWVzLWF1dG8iLCJzdWIiOiI1YjM4NGIyY2Q4NzEyMDY0ZjEwMWUzMWUifQ.GxC31Tm9LBE9DBplNhR4GD7PH8M63smNh3MmY5OUZDhI9S8T4n2FaflzHj1mSO32nU8_l4rxfd7ct2A3eVZtUhaSLxbyUSXV3zzx3_xNDZ-c1ttiZkNQU6rXxGB_QwSCLrsVEmGt97AKZWvlc8nVBl8BfDLv5xd2UFInl7Bi5DMiJvcP8LIyl3uu4ES0itVp_45SUkY2Uwj6WBB5rvpuk06lLzdvmQhfFcT81Krr08tj8jOryVJc6uzV4YAmOA5O_5gYrkhnePtmmHmfxmZphQ94o-8VGZsVJr429GyF3qib-9ulXaq4UAZToHwVrh6vUCijeDr73jokjC2c0hch7g';
    const tokenReq = request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next.handle(tokenReq);
  }
}


/** Http interceptor providers in outside-in order */
export const httpInterceptorProviders = [
  {provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true}
];

/*
 * For easier debugging in development mode, you can import the following file
 * to ignore zone related error stack frames such as `zone.run`, `zoneDelegate.invokeTask`.
 *
 * This import should be commented out in production mode because it will have a negative impact
 * on performance if an error is thrown.
 */
// import 'zone.js/dist/zone-error';  // Included with Angular CLI.
