import {Batch} from './batch';
import {LineMachine} from './line-machine';

export class ProductPlanNotify {
  id: string;
  batch: Batch;
  type: string;
  name: string;
  startDate: Date;
  lineMachines: LineMachine[];

  static assign(...sources: any[]): ProductPlanNotify {
    const result = Object.assign(new ProductPlanNotify(), ...sources);
    return result;
  }

  static toEntities(os: ProductPlanNotify[], entities?: { [id: string]: ProductPlanNotify }): { [id: string]: ProductPlanNotify } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = ProductPlanNotify.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
