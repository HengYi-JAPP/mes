import {LoggableEntity} from './loggable-entity';

export class Corporation extends LoggableEntity {
  id: string;
  name: string;
  code: string;

  static assign(...sources: any[]): Corporation {
    const result = Object.assign(new Corporation(), ...sources);
    return result;
  }

  static toEntities(os: Corporation[], entities?: { [id: string]: Corporation }): { [id: string]: Corporation } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Corporation.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
