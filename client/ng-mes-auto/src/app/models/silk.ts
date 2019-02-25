import {Batch} from './batch';
import {Grade} from './grade';
import {LineMachine} from './line-machine';

export class Silk {
  id: string;
  code: string;
  batch: Batch;
  lineMachine: LineMachine;
  spindle: number;
  grade: Grade;

  static assign(...sources: any[]): Silk {
    const result = Object.assign(new Silk(), ...sources);
    return result;
  }

  static toEntities(os: Silk[], entities?: { [id: string]: Silk }): { [id: string]: Silk } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Silk.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
