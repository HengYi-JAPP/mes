import {createSelector} from '@ngrx/store';
import {Product} from '../../models/product';
import {Actions, ProductConfgPageActionTypes} from '../actions/product-config-page';

export interface State {
  productEntities: { [id: string]: Product };
  id?: string;
}

const initialState: State = {
  productEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case ProductConfgPageActionTypes.InitSuccess: {
      const {products, id} = action.payload;
      const productEntities = Product.toEntities(products);
      return {...state, productEntities, id};
    }

    case ProductConfgPageActionTypes.SaveSuccess: {
      const {product} = action.payload;
      const productEntities = Product.toEntities([product], state.productEntities);
      return {...state, productEntities};
    }

    default:
      return state;
  }
}

export const getProductEntities = (state: State) => state.productEntities;
const getId = (state: State) => state.id;
export const getProducts = createSelector(getProductEntities, (entities) => Object.values(entities));
export const getProduct = createSelector(getProductEntities, getId, (entities, id) => id ? entities[id] : null);
