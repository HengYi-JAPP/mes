import {createSelector} from '@ngrx/store';
import {Workshop} from '../../models/workshop';
import {CheckQ} from '../../services/util.service';
import {Actions, WorkshopManagePageActionTypes} from '../actions/workshop-manage-page';

export interface State {
  workshopEntities: { [id: string]: Workshop };
  q?: string;
}

const initialState: State = {
  workshopEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case WorkshopManagePageActionTypes.InitSuccess: {
      const {workshops} = action.payload;
      const workshopEntities = Workshop.toEntities(workshops);
      return {...state, workshopEntities, q: null};
    }

    case WorkshopManagePageActionTypes.SaveSuccess: {
      const {workshop} = action.payload;
      const workshopEntities = Workshop.toEntities([workshop], state.workshopEntities);
      return {...state, workshopEntities};
    }

    case WorkshopManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const workshopEntities = {...state.workshopEntities};
      delete workshopEntities[id];
      return {...state, workshopEntities};
    }

    case WorkshopManagePageActionTypes.SetQ: {
      const {q} = action.payload;
      return {...state, q: q};
    }

    default:
      return state;
  }
}

export const getWorkshopEntities = (state: State) => state.workshopEntities;
export const getQ = (state: State) => state.q;
export const getWorkshops = createSelector(getWorkshopEntities, getQ, (entities, q) =>
  Object.values(entities)
    .filter(it => CheckQ(it.name, q))
);
