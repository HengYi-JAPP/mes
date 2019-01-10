import {Line} from './line';
import {LineMachineProductPlan} from './line-machine-product-plan';

export class LineMachine {
  id: string;
  line: Line;
  item: number;
  spindleNum: number;
  spindleSeq: number[];
  productPlan: LineMachineProductPlan;

  static assign(...sources: any[]): LineMachine {
    const result = Object.assign(new LineMachine(), ...sources);
    return result;
  }

  static toEntities(os: LineMachine[], entities?: { [id: string]: LineMachine }): { [id: string]: LineMachine } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = LineMachine.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
