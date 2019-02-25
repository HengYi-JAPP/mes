import {LoggableEntity} from './loggable-entity';

export class SilkException extends LoggableEntity {
  id: string;
  name: string;

  static assign(...sources: any[]): SilkException {
    const result = Object.assign(new SilkException(), ...sources);
    return result;
  }

  static toEntities(os: SilkException[], entities?: { [id: string]: SilkException }): { [id: string]: SilkException } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = SilkException.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
