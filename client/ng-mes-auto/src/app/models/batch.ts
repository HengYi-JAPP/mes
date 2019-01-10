import {Product} from './product';
import {Workshop} from './workshop';

export class Batch {
  id: string;
  workshop: Workshop;
  product: Product;
  batchNo: string;
  centralValue: number;
  silkWeight: number;
  holeNum: number;
  spec: string;
  tubeColor: string;
  note: string;

  static assign(...sources: any[]): Batch {
    const result = Object.assign(new Batch(), ...sources);
    return result;
  }

  static toEntities(os: Batch[], entities?: { [id: string]: Batch }): { [id: string]: Batch } {
    return (os || []).reduce((acc, cur) => {
      acc[cur.id] = Batch.assign(cur);
      return acc;
    }, {...(entities || {})});
  }
}
