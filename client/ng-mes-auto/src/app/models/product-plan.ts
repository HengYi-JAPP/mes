import {Batch} from './batch';

export class ProductPlan {
  id: string;
  type: string;
  batch: Batch;
  startDate: Date;
  endDate: Date;

  static assign(...sources: any[]): ProductPlan {
    const result = Object.assign(new ProductPlan(), ...sources);
    return result;
  }

  static toEntities(os: ProductPlan[], entities?: { [id: string]: ProductPlan }): { [id: string]: ProductPlan } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = ProductPlan.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
