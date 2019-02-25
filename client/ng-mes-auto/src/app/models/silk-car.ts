export class SilkCar {
  id: string;
  type: string;
  number: string;
  code: string;
  row: number;
  col: number;

  static assign(...sources: any[]): SilkCar {
    const result = Object.assign(new SilkCar(), ...sources);
    return result;
  }

  static toEntities(os: SilkCar[], entities?: { [id: string]: SilkCar }): { [id: string]: SilkCar } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = SilkCar.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
