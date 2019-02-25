import {Silk} from './silk';

export class SilkRuntime {
  silk: Silk;
  sideType: string;
  row: number;
  col: number;

  static assign(...sources: any[]): SilkRuntime {
    const result = Object.assign(new SilkRuntime(), ...sources);
    return result;
  }

  static toEntities(os: SilkRuntime[], entities?: { [id: string]: SilkRuntime }): { [id: string]: SilkRuntime } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.silk.id] = SilkRuntime.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
