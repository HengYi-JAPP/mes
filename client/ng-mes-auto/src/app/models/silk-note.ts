import {LoggableEntity} from './loggable-entity';

export class SilkNote extends LoggableEntity {
  id: string;
  name: string;

  static assign(...sources: any[]): SilkNote {
    const result = Object.assign(new SilkNote(), ...sources);
    return result;
  }

  static toEntities(os: SilkNote[], entities?: { [id: string]: SilkNote }): { [id: string]: SilkNote } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = SilkNote.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
