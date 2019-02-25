import {createSelector} from '@ngrx/store';
import {SilkCar} from '../../models/silk-car';
import {Actions, SilkCarManagePageActionTypes} from '../actions/silk-car-manage-page';

export class State {
  count = 0;
  first = 0;
  pageSize = 0;
  q = '';
  silkCarEntities: { [id: string]: SilkCar } = {};
}

export function reducer(state = new State(), action: Actions): State {
  switch (action.type) {
    case SilkCarManagePageActionTypes.InitSuccess: {
      const {silkCars, count, pageSize, first, q} = action.payload;
      const silkCarEntities = SilkCar.toEntities(silkCars);
      return {...state, silkCarEntities, count, pageSize, first, q};
    }

    case SilkCarManagePageActionTypes.SaveSuccess: {
      const {silkCar} = action.payload;
      const silkCarEntities = SilkCar.toEntities([silkCar], state.silkCarEntities);
      return {...state, silkCarEntities};
    }

    case SilkCarManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const silkCarEntities = {...state.silkCarEntities};
      delete silkCarEntities[id];
      return {...state, silkCarEntities};
    }

    default:
      return state;
  }
}

export const getSilkCarEntities = (state: State) => state.silkCarEntities;
export const getFirst = (state: State) => state.first;
export const getCount = (state: State) => state.count;
export const getPageSize = (state: State) => state.pageSize;
export const getQ = (state: State) => state.q;
export const getPageIndex = createSelector(getFirst, getPageSize, (first, pageSize) => first / pageSize);
export const getSilkCars = createSelector(getSilkCarEntities, entities => Object.values(entities));
