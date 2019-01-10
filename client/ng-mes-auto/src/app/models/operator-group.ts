import {Operator} from './operator';
import {Permission} from './permission';

export class OperatorGroup {
  id: string;
  name: string;
  roles: string[];
  permissions: Permission[];
  operators: Operator[];

  static assign(...sources: any[]): OperatorGroup {
    const result = Object.assign(new OperatorGroup(), ...sources);
    return result;
  }

  static toEntities(os: OperatorGroup[], entities?: { [id: string]: OperatorGroup }): { [id: string]: OperatorGroup } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = OperatorGroup.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
