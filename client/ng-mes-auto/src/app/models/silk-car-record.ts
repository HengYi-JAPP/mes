import {Batch} from './batch';
import {EventSourceTypes} from './event-source';
import {Operator} from './operator';
import {SilkCar} from './silk-car';
import {SilkRuntime} from './silk-runtime';

export class SilkCarRecord {
  id: string;
  silkCar: SilkCar;
  batch: Batch;
  doffingOperator: Operator;
  doffingType: string;
  doffingDateTime: Date;
  carpoolOperator: Operator;
  carpoolDateTime: Date;
  initSilkRuntimes: SilkRuntime[];
  initEventSource: EventSourceTypes;
  eventSources: EventSourceTypes[];

  static assign(...sources: any[]): SilkCarRecord {
    const result = Object.assign(new SilkCarRecord(), ...sources);
    return result;
  }

  static toEntities(os: SilkCarRecord[], entities?: { [id: string]: SilkCarRecord }): { [id: string]: SilkCarRecord } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = SilkCarRecord.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
