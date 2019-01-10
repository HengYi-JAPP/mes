import {createSelector} from '@ngrx/store';
import {AuthInfo} from '../../models/auth-info';
import {Operator} from '../../models/operator';
import {Actions, CoreActionTypes} from '../actions/core';

export class State {
  loading = false;
  authInfo?: AuthInfo;
}

export function reducer(state = new State(), action: Actions): State {
  switch (action.type) {
    case CoreActionTypes.FetchAuthInfoSuccess: {
      const {authInfo} = action.payload;
      return {...state, authInfo};
    }

    case CoreActionTypes.SetLoading: {
      return {...state, loading: action.payload};
    }

    default:
      return state;
  }
}

export const getLoading = (state: State) => state.loading;
export const getAuthInfo = (state: State) => state.authInfo;
export const getAuthOperator = createSelector(getAuthInfo, it => Operator.assign(it));
export const getAuthAdmin = createSelector(getAuthInfo, it => it && it.admin);
