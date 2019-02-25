import {createSelector} from '@ngrx/store';
import {Permission} from '../../models/permission';
import {Actions, PermissionManagePageActionTypes} from '../actions/permission-manage-page';

export interface State {
  permissionEntities: { [id: string]: Permission };
}

const initialState: State = {
  permissionEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case PermissionManagePageActionTypes.InitSuccess: {
      const {permissions} = action.payload;
      const permissionEntities = Permission.toEntities(permissions);
      return {...state, permissionEntities};
    }

    case PermissionManagePageActionTypes.SaveSuccess: {
      const {permission} = action.payload;
      const permissionEntities = Permission.toEntities([permission], state.permissionEntities);
      return {...state, permissionEntities};
    }

    case PermissionManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const permissionEntities = {...state.permissionEntities};
      delete permissionEntities[id];
      return {...state, permissionEntities};
    }

    default:
      return state;
  }
}

export const getPermissionEntities = (state: State) => state.permissionEntities;
export const getPermissions = createSelector(getPermissionEntities, entities =>
  Object.values(entities)
);
