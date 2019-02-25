import {createSelector} from '@ngrx/store';
import {OperatorGroup} from '../../models/operator-group';
import {CheckQ} from '../../services/util.service';
import {Actions, OperatorGroupManagePageActionTypes} from '../actions/operator-group-manage-page';

export interface State {
  operatorGroupEntities: { [id: string]: OperatorGroup };
  q?: string;
}

const initialState: State = {
  operatorGroupEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case OperatorGroupManagePageActionTypes.InitSuccess: {
      const {operatorGroups, q} = action.payload;
      const corporationEntities = OperatorGroup.toEntities(operatorGroups);
      return {...state, operatorGroupEntities: corporationEntities, q: q};
    }

    case OperatorGroupManagePageActionTypes.SaveSuccess: {
      const {operatorGroup} = action.payload;
      const corporationEntities = OperatorGroup.toEntities([operatorGroup], state.operatorGroupEntities);
      return {...state, operatorGroupEntities: corporationEntities};
    }

    case OperatorGroupManagePageActionTypes.SetQ: {
      const {q} = action.payload;
      return {...state, q: q};
    }

    default:
      return state;
  }
}

export const getOperatorGroupEntities = (state: State) => state.operatorGroupEntities;
export const getQ = (state: State) => state.q;
export const getOperatorGroups = createSelector(getOperatorGroupEntities, getQ, (entities, q) =>
  Object.values(entities)
    .filter(it => CheckQ(it.name, q))
);
