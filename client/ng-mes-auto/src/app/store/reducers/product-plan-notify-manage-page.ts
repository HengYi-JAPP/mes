import {createSelector} from '@ngrx/store';
import {ProductPlanNotify} from '../../models/product-plan-notify';
import {Actions, ProductPlanNotifyManagePageActionTypes} from '../actions/product-plan-notify-manage-page';

export interface State {
  productPlanNotifyEntities: { [id: string]: ProductPlanNotify };
  count?: number;
  first?: number;
  pageSize?: number;
  q?: string;
}

const initialState: State = {
  productPlanNotifyEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case ProductPlanNotifyManagePageActionTypes.InitSuccess: {
      const {productPlanNotifies, count, pageSize, first, q} = action.payload;
      const productPlanNotifyEntities = ProductPlanNotify.toEntities(productPlanNotifies);
      return {...state, productPlanNotifyEntities, count, pageSize, first, q};
    }

    case ProductPlanNotifyManagePageActionTypes.SaveSuccess: {
      const {productPlanNotify} = action.payload;
      const productPlanNotifyEntities = ProductPlanNotify.toEntities([productPlanNotify], state.productPlanNotifyEntities);
      return {...state, productPlanNotifyEntities};
    }

    default:
      return state;
  }
}

export const getProductPlanNotifyEntities = (state: State) => state.productPlanNotifyEntities;
export const getFirst = (state: State) => state.first;
export const getPageSize = (state: State) => state.pageSize;
export const getCount = (state: State) => state.count;
export const getQ = (state: State) => state.q;
export const getPageIndex = createSelector(getFirst, getPageSize, (first, pageSize) => first / pageSize);
export const getProductPlanNotifies = createSelector(getProductPlanNotifyEntities, entities => Object.values(entities));
