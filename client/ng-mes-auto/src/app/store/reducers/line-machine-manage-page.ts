import {createSelector} from '@ngrx/store';
import {Line} from '../../models/line';
import {LineMachine} from '../../models/line-machine';
import {LineCompare, LineMachineCompare} from '../../services/util.service';
import {Actions, LineMachineManagePageActionTypes} from '../actions/line-machine-manage-page';

export class State {
  lineMachineEntities: { [id: string]: LineMachine } = {};
  lineEntities: { [id: string]: Line } = {};
  lineId?: string;
}

export function reducer(state = new State(), action: Actions): State {
  switch (action.type) {
    case LineMachineManagePageActionTypes.InitSuccess: {
      const {lineId, lines, lineMachines} = action.payload;
      const lineMachineEntities = LineMachine.toEntities(lineMachines);
      const lineEntities = Line.toEntities(lines);
      return {...state, lineId, lineMachineEntities, lineEntities};
    }

    case LineMachineManagePageActionTypes.SaveSuccess: {
      const {lineMachine} = action.payload;
      const lineMachineEntities = LineMachine.toEntities([lineMachine], state.lineMachineEntities);
      return {...state, lineMachineEntities};
    }

    case LineMachineManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const lineMachineEntities = {...state.lineMachineEntities};
      delete lineMachineEntities[id];
      return {...state, lineMachineEntities};
    }

    default:
      return state;
  }
}

export const getLinesEntities = (state: State) => state.lineEntities;
export const getLineId = (state: State) => state.lineId;
export const getLines = createSelector(getLinesEntities, entities =>
  Object.values(entities)
    .sort(LineCompare)
);
export const getLine = createSelector(getLinesEntities, getLineId, (entities, id) => id ? entities[id] : null);
export const getLineMachineEntities = (state: State) => state.lineMachineEntities;
export const getLineMachines = createSelector(getLineMachineEntities, entities =>
  Object.values(entities)
    .sort(LineMachineCompare)
);
