import {Batch} from './batch';
import {LineMachine} from './line-machine';
import {ProductPlanNotify} from './product-plan-notify';

export class LineMachineProductPlan {
  id: string;
  productPlanNotify: ProductPlanNotify;
  lineMachine: LineMachine;
  batch: Batch;
  startDate: Date;
  endDate: Date;
  prev: LineMachineProductPlan;
  next: LineMachineProductPlan;

  static assign(...sources: any[]): LineMachineProductPlan {
    const result = Object.assign(new LineMachineProductPlan(), ...sources);
    return result;
  }

  static toEntities(os: LineMachineProductPlan[], entities?: { [id: string]: LineMachineProductPlan }): { [id: string]: LineMachineProductPlan } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = LineMachineProductPlan.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
