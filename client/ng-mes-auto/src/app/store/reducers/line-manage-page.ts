import {createSelector} from '@ngrx/store';
import {Line} from '../../models/line';
import {Workshop} from '../../models/workshop';
import {CheckQ, LineCompare} from '../../services/util.service';
import {Actions, LineManagePageActionTypes} from '../actions/line-manage-page';

export interface State {
  lineEntities: { [id: string]: Line };
  workshopEntities: { [id: string]: Workshop };
  workshopId?: string;
  q?: string;
}

const initialState: State = {
  lineEntities: {},
  workshopEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case LineManagePageActionTypes.InitSuccess: {
      const {workshopId, workshops, lines} = action.payload;
      const lineEntities = Line.toEntities(lines);
      const workshopEntities = Workshop.toEntities(workshops);
      return {...state, lineEntities, workshopId, workshopEntities, q: null};
    }

    case LineManagePageActionTypes.SaveSuccess: {
      const {line} = action.payload;
      const lineEntities = Line.toEntities([line], state.lineEntities);
      return {...state, lineEntities};
    }

    case LineManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const lineEntities = {...state.lineEntities};
      delete lineEntities[id];
      return {...state, lineEntities};
    }

    case LineManagePageActionTypes.SetQ: {
      const {q} = action.payload;
      return {...state, q: q};
    }

    default:
      return state;
  }
}

export const getLinesEntities = (state: State) => state.lineEntities;
export const getWorkshopEntities = (state: State) => state.workshopEntities;
export const getQ = (state: State) => state.q;
export const getWorkshopId = (state: State) => state.workshopId;
export const getWorkshops = createSelector(getWorkshopEntities, entities => Object.values(entities));
export const getWorkshop = createSelector(getWorkshopEntities, getWorkshopId, (entities, id) => id ? entities[id] : null);
export const getLines = createSelector(getLinesEntities, getQ, (entities, q) =>
  Object.values(entities)
    .filter(it => CheckQ(it.name, q))
    .sort(LineCompare)
);
