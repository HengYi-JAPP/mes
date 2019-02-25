import {ProductPlanNotify} from '../../models/product-plan-notify';
import {Actions, ProductPlanNotifyExeInfoPageActionTypes} from '../actions/product-plan-notify-exe-info-page';

export class State {
  productPlanNotify: ProductPlanNotify;
}

export function reducer(state = new State(), action: Actions): State {
  switch (action.type) {
    case ProductPlanNotifyExeInfoPageActionTypes.InitSuccess: {
      const {productPlanNotify, lineMachineProductPlans} = action.payload;
      const {lineMachines} = productPlanNotify;
      lineMachineProductPlans.forEach(productPlan => {
        const {lineMachine: {id}} = productPlan;
        const find = lineMachines.find(it => it.id === id);
        find.productPlan = productPlan;
      });
      return {...state, productPlanNotify};
    }

    default:
      return state;
  }
}

export const getProductPlanNotify = (state: State) => state.productPlanNotify;
