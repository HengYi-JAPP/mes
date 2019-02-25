import {Batch} from './batch';
import {Grade} from './grade';
import {Line} from './line';
import {Workshop} from './workshop';

export class StatisticsReport {
  workshop: Workshop;
  startDate: Date;
  endDate: Date;
  items: Item[];
}

export class Item {
  line: Line;
  batch: Batch;
  grade: Grade;

  silkCount: number;
  silkWeight: number;
}
