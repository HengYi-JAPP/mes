import {Corporation} from './corporation';
import {LoggableEntity} from './loggable-entity';

export class Workshop extends LoggableEntity {
  id: string;
  name: string;
  note: string;
  corporation: Corporation;

  static assign(...sources: any[]): Workshop {
    const result = Object.assign(new Workshop(), ...sources);
    return result;
  }

  static toEntities(os: Workshop[], entities?: { [id: string]: Workshop }): { [id: string]: Workshop } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Workshop.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
