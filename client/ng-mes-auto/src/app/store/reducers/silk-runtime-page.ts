import {createSelector} from '@ngrx/store';
import {EventSourceTypes} from '../../models/event-source';
import {SilkCarRuntime} from '../../models/silk-car-runtime';
import {EventSourceCompare} from '../../services/util.service';
import {Actions, SilkRuntimePageActionTypes} from '../actions/silk-runtime-page';

export interface State {
  silkCarRuntime?: SilkCarRuntime;
  fireDateTimeSortDirection?: string;
}

const initialState: State = {
  fireDateTimeSortDirection: 'desc'
};

export function reducer(state = initialState, action: Actions): State {
  switch (action.type) {
    case SilkRuntimePageActionTypes.FetchSilkCarSuccess: {
      const {silkCarRuntime} = action.payload;
      return {...state, silkCarRuntime};
    }

    case SilkRuntimePageActionTypes.SetFireDateTimeSort: {
      const {direction} = action.payload;
      return {...state, fireDateTimeSortDirection: direction};
    }

    case SilkRuntimePageActionTypes.EventSourceSockjsRec: {
      const {sockjsRec: {payload: {body}}} = action.payload;
      const silkCarRuntime = SilkCarRuntime.assign(state.silkCarRuntime);
      silkCarRuntime.eventSources = [body].concat(silkCarRuntime.eventSources || []);
      return {...state, silkCarRuntime};
    }

    default:
      return state;
  }
}

export const getSilkCarRuntime = (state: State) => state.silkCarRuntime;
export const getFireDateTimeSortDirection = (state: State) => state.fireDateTimeSortDirection;

export const getSilkCarRecord = createSelector(getSilkCarRuntime, it => it && it.silkCarRecord);
export const getEventSources = createSelector(getSilkCarRuntime, getFireDateTimeSortDirection, (silkCarRuntime, direction) => {
  const eventSources: EventSourceTypes[] = silkCarRuntime && silkCarRuntime.eventSources || [];
  const isDesc = direction === 'desc';
  return [...eventSources].sort((o1, o2) => {
    const i = EventSourceCompare(o1, o2);
    return isDesc ? i : (i * -1);
  });
});
