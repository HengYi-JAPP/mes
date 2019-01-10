import {createSelector} from '@ngrx/store';
import {Product} from '../../models/product';
import {Actions, ProductManagePageActionTypes} from '../actions/product-manage-page';

export interface State {
  productEntities: { [id: string]: Product };
}

const initialState: State = {
  productEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case ProductManagePageActionTypes.InitSuccess: {
      const {products} = action.payload;
      const productEntities = Product.toEntities(products);
      return {...state, productEntities};
    }

    case ProductManagePageActionTypes.SaveSuccess: {
      const {product} = action.payload;
      const productEntities = Product.toEntities([product], state.productEntities);
      return {...state, productEntities};
    }

    default:
      return state;
  }
}

export const getProductEntities = (state: State) => state.productEntities;
export const getProducts = createSelector(getProductEntities, (entities) => Object.values(entities));
