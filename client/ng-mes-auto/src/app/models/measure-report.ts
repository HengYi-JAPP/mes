import {Batch} from './batch';
import {Grade} from './grade';
import {PackageClass} from './package-class';
import {Workshop} from './workshop';

export class MeasureReport {
  workshop: Workshop;
  date: Date;
  budatClass: PackageClass;
  items: Item[];
}

export class Item {
  batch: Batch;
  grade: Grade;

  sumPackageBoxCount: number;
  domesticPackageBoxCount: number;
  foreignPackageBoxCount: number;

  sumSilkCount: number;
  domesticSilkCount: number;
  foreignSilkCount: number;

  sumNetWeight: number;
  domesticNetWeight: number;
  foreignNetWeight: number;

  sumFoamCount: number;
  domesticFoamCount: number;
  foreignFoamCount: number;
}
