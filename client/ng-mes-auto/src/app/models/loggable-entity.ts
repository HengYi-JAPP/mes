import {Operator} from './operator';

export class LoggableEntity {
  creator: Operator;
  createDateTime: Date;
  modifier: Operator;
  modifyDateTime: Date;
}
