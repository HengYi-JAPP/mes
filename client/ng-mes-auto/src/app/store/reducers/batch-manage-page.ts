import {createSelector} from '@ngrx/store';
import {Batch} from '../../models/batch';
import {Actions, BatchManagePageActionTypes} from '../actions/batch-manage-page';

export interface State {
  q?: string;
  count: number;
  first: number;
  pageSize: number;
  batchEntities: { [id: string]: Batch };
}

const initialState: State = {
  first: 0,
  count: 0,
  pageSize: 0,
  batchEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case BatchManagePageActionTypes.InitSuccess: {
      const {batches, count, pageSize, first, q} = action.payload;
      const batchEntities = Batch.toEntities(batches);
      return {...state, batchEntities, count, pageSize, first, q};
    }

    case BatchManagePageActionTypes.SaveSuccess: {
      const {batch} = action.payload;
      const batchEntities = Batch.toEntities([batch], state.batchEntities);
      return {...state, batchEntities};
    }

    case BatchManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const batchEntities = {...state.batchEntities};
      delete batchEntities[id];
      return {...state, batchEntities};
    }

    default:
      return state;
  }
}

export const getBatchEntities = (state: State) => state.batchEntities;
export const getFirst = (state: State) => state.first;
export const getCount = (state: State) => state.count;
export const getPageSize = (state: State) => state.pageSize;
export const getQ = (state: State) => state.q;
export const getPageIndex = createSelector(getFirst, getPageSize, (first, pageSize) => first / pageSize);
export const getBatches = createSelector(getBatchEntities, entities => Object.values(entities)
);
