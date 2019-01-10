import {FormConfig} from './form-config';
import {Operator} from './operator';
import {ProductProcess} from './product-process';
import {SilkCarRecord} from './silk-car-record';
import {SilkException} from './silk-exception';
import {SilkNote} from './silk-note';
import {SilkRuntime} from './silk-runtime';

export class EventSource {
  type: string;
  eventId: string;
  operator: Operator;
  fireDateTime: Date;
}

export class ProductProcessSubmitEvent extends EventSource {
  silkCarRecord: SilkCarRecord;
  silkRuntimes: SilkRuntime[];
  productProcess: ProductProcess;
  silkExceptions: SilkException[];
  silkNotes: SilkNote[];
  formConfig: FormConfig;
  formConfigValueData: { [id: string]: any };
}

export class DyeingSampleSilkSubmitEvent extends EventSource {
  silkCarRecord: SilkCarRecord;
  silkRuntimes: SilkRuntime[];
}

export class SilkRuntimeDetachEvent extends EventSource {
  silkCarRecord: SilkCarRecord;
  silkRuntimes: SilkRuntime[];
}

export type EventSourceTypes =
  | DyeingSampleSilkSubmitEvent
  | SilkRuntimeDetachEvent
  | ProductProcessSubmitEvent;
