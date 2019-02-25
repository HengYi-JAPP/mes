import {LoggableEntity} from './loggable-entity';

export class PackageClass extends LoggableEntity {
  id: string;
  name: string;
  sortBy: number;

  static assign(...sources: any[]): PackageClass {
    const result = Object.assign(new PackageClass(), ...sources);
    return result;
  }

  static toEntities(os: PackageClass[], entities?: { [id: string]: PackageClass }): { [id: string]: PackageClass } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = PackageClass.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
