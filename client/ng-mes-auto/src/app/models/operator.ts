import {OperatorGroup} from './operator-group';
import {Permission} from './permission';

export class Operator {
  id: string;
  name: string;
  hrId: string;
  admin: boolean;
  roles: string[];
  groups: OperatorGroup[];
  permissions: Permission[];

  static assign(...sources: any[]): Operator {
    const result = Object.assign(new Operator(), ...sources);
    return result;
  }

  static toEntities(os: Operator[], entities?: { [id: string]: Operator }): { [id: string]: Operator } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Operator.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
