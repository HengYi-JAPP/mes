import {LineCompare, LineMachineCompare} from '../services/util.service';
import {Batch} from './batch';
import {Line} from './line';
import {LineMachine} from './line-machine';
import {Workshop} from './workshop';

export class WorkshopProductPlanReport {
  workshop: Workshop;
  items: Item[];

  static assign(...sources: any[]): WorkshopProductPlanReport {
    const result = Object.assign(new WorkshopProductPlanReport(), ...sources);
    result.items = (result.items || []).map(it => Item.assign(it))
      .sort((o1, o2) => LineCompare(o1.line, o2.line));
    return result;
  }
}

class LineMachineSpec {
  start: number;
  end: number;

  constructor(i: number) {
    this.start = this.end = i;
  }
}

export class Item {
  line: Line;
  batch: Batch;
  lineMachines: LineMachine[];
  private _lineMachineSpecs: string[];

  get lineMachineSpecs(): string[] {
    if (this._lineMachineSpecs) {
      return this._lineMachineSpecs;
    }

    if (this.lineMachineCount < 1) {
      return [];
    }
    const specs: LineMachineSpec[] = [];
    let spec: LineMachineSpec;
    this.lineMachines.sort(LineMachineCompare)
      .forEach(lineMachine => {
        if (!spec) {
          spec = new LineMachineSpec(lineMachine.item);
        } else if ((lineMachine.item - spec.end) === 1) {
          spec.end = lineMachine.item;
        } else {
          specs.push(spec);
          spec = new LineMachineSpec(lineMachine.item);
        }
      });
    if (spec) {
      specs.push(spec);
    }
    this._lineMachineSpecs = specs.map(it => it.start === it.end ? `${it.start}#` : `${it.start}#—${it.end}#`);
    return this._lineMachineSpecs;
  }

  get lineMachineCount(): number {
    return this.lineMachines && this.lineMachines.length;
  }

  static assign(...sources: any[]): Item {
    const result = Object.assign(new Item(), ...sources);
    return result;
  }

}
