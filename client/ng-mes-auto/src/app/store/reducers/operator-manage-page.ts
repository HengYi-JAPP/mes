import {createSelector} from '@ngrx/store';
import {Operator} from '../../models/operator';
import {Actions, OperatorManageActionTypes} from '../actions/operator-manage-page';

export interface State {
  operatorEntities: { [id: string]: Operator };
  count?: number;
  first?: number;
  pageSize?: number;
  q?: string;
}

const initialState: State = {
  operatorEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case OperatorManageActionTypes.InitSuccess: {
      const {operators, count, pageSize, first, q} = action.payload;
      const operatorEntities = Operator.toEntities(operators);
      return {...state, operatorEntities, count, pageSize, first, q};
    }

    case OperatorManageActionTypes.SaveSuccess: {
      const {operator} = action.payload;
      const operatorEntities = Operator.toEntities([operator], state.operatorEntities);
      return {...state, operatorEntities};
    }

    default:
      return state;
  }
}

export const getOperatorEntities = (state: State) => state.operatorEntities;
export const getFirst = (state: State) => state.first;
export const getPageSize = (state: State) => state.pageSize;
export const getCount = (state: State) => state.count;
export const getQ = (state: State) => state.q;
export const getPageIndex = createSelector(getFirst, getPageSize, (first, pageSize) => first / pageSize);
export const getOperators = createSelector(getOperatorEntities, entities => Object.values(entities));
