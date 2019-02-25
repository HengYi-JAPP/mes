import {createSelector} from '@ngrx/store';
import {Grade} from '../../models/grade';
import {Actions, GradeManagePageActionTypes} from '../actions/grade-manage-page';

export interface State {
  gradeEntities: { [id: string]: Grade };
}

const initialState: State = {
  gradeEntities: {}
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case GradeManagePageActionTypes.InitSuccess: {
      const {grades} = action.payload;
      const gradeEntities = Grade.toEntities(grades);
      return {...state, gradeEntities};
    }

    case GradeManagePageActionTypes.SaveSuccess: {
      const {grade} = action.payload;
      const gradeEntities = Grade.toEntities([grade], state.gradeEntities);
      return {...state, gradeEntities};
    }

    case GradeManagePageActionTypes.DeleteSuccess: {
      const {id} = action.payload;
      const gradeEntities = {...state.gradeEntities};
      delete gradeEntities[id];
      return {...state, gradeEntities};
    }

    default:
      return state;
  }
}

export const getGradeEntities = (state: State) => state.gradeEntities;
export const getGrades = createSelector(getGradeEntities, entities =>
  Object.values(entities)
    .sort((o1, o2) => o2.sortBy - o1.sortBy)
);
