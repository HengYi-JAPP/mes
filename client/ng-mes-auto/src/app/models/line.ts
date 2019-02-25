import {Product} from './product';
import {Workshop} from './workshop';

export class Line {
  id: string;
  workshop: Workshop;
  name: string;
  product: Product;
  doffingType: string;

  static assign(...sources: any[]): Line {
    const result = Object.assign(new Line(), ...sources);
    return result;
  }

  static toEntities(os: Line[], entities?: { [id: string]: Line }): { [id: string]: Line } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Line.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
