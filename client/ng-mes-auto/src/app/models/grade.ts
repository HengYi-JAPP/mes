import {LoggableEntity} from './loggable-entity';

export class Grade extends LoggableEntity {
  id: string;
  name: string;
  sortBy: number;

  static assign(...sources: any[]): Grade {
    const result = Object.assign(new Grade(), ...sources);
    return result;
  }

  static toEntities(os: Grade[], entities?: { [id: string]: Grade }): { [id: string]: Grade } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Grade.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
