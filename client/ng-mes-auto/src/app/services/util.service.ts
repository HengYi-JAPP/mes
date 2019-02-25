import {ComponentType} from '@angular/cdk/portal';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {ObservableMedia} from '@angular/flex-layout';
import {AbstractControl} from '@angular/forms';
import {ValidationErrors} from '@angular/forms/src/directives/validators';
import {MatDialog, MatSnackBar, MatSnackBarConfig} from '@angular/material';
import {RouterNavigationAction} from '@ngrx/router-store';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import * as moment from 'moment';
import {Observable} from 'rxjs';
import {filter} from 'rxjs/operators';
import {isArray, isNullOrUndefined, isObject, isString} from 'util';
import {ConfirmDialogComponent} from '../components/confirm-dialog/confirm-dialog.component';
import {EventSourceTypes} from '../models/event-source';
import {Line} from '../models/line';
import {LineMachine} from '../models/line-machine';
import {ProductProcess} from '../models/product-process';

@Injectable({
  providedIn: 'root'
})
export class UtilService {
  constructor(private http: HttpClient,
              private store: Store<any>,
              private translate: TranslateService,
              private media$: ObservableMedia,
              private dialog: MatDialog,
              private snackBar: MatSnackBar) {
  }

  showSuccess(message?: string, action?: string, config?: MatSnackBarConfig) {
    config = config || {duration: 3000};
    message = message || 'Toast.success';
    this.translate.get(message).subscribe(res => this.snackBar.open(res, action, config));
  }

  showError(err: string | any, action?: string, config?: MatSnackBarConfig) {
    console.log('UtilService', err);
    if (isString(err)) {
      this.translate.get(err).subscribe(res => this.snackBar.open(res, action || 'X', config));
      return;
    } else if (err instanceof HttpErrorResponse) {
      if (err.error && err.error.errorCode) {
        this.translate.get(`Error.${err.error.errorCode}`)
          .subscribe(it => this.showError(it));
      }
      if (err.error && err.error.errorMessage) {
        this.translate.get(err.error.errorMessage)
          .subscribe(it => this.showError(it));
      }
    } else if (err instanceof Error) {
      const error = err as Error;
      this.translate.get(error.message)
        .subscribe(it => this.showError(it));
    }
  }

  showConfirm(data?: any): Observable<boolean> {
    return ConfirmDialogComponent.open(this.dialog, data)
      .afterClosed()
      .pipe(
        filter(res => res)
      );
  }

}

export const PAGE_SIZE_OPTIONS = [20, 50, 100];

export const requiredPickEntity = (ctrl: AbstractControl): ValidationErrors | null => {
  const {value} = ctrl;
  if (!value) {
    return null;
  }
  if (value.id) {
    return null;
  }
  return {'requiredPickEntity': true};
};

export const compareWithId = (o1: any, o2: any) => {
  if (o1 === o2) {
    return true;
  }
  if (isNullOrUndefined(o1) || isNullOrUndefined(o2)) {
    return false;
  }
  return o1.id === o2.id;
};

export const upEle = <T>(array: T[], ele: T): T[] => {
  if (!array || !ele || !isArray(array)) {
    return array;
  }
  array = [...(array || [])];
  const i = array.findIndex(it => {
    if (it === ele) {
      return true;
    }
    if (isObject(it)) {
      return (it && it['id']) === ele['id'];
    }
    return false;
  });
  if (i <= 0) {
    return array;
  }
  array[i] = array[i - 1];
  array[i - 1] = ele;
  return array;
};

export const downEle = <T>(array: T[], ele: T): T[] => {
  if (!array || !ele || !isArray(array)) {
    return array;
  }
  array = [...(array || [])];
  const i = array.findIndex(it => {
    if (it === ele) {
      return true;
    }
    if (isObject(it)) {
      return (it && it['id']) === ele['id'];
    }
    return false;
  });
  if (i < 0 || i === (array.length - 1)) {
    return array;
  }
  array[i] = array[i + 1];
  array[i + 1] = ele;
  return array;
};

export const defaultValue = (v: any, d: any): any => {
  return isNullOrUndefined(v) ? d : v;
};

export const CheckQ = (sV: string, qV: string): boolean => {
  let s = sV || '';
  let q = qV || '';
  s = s.toLocaleLowerCase();
  q = q.toLocaleLowerCase();
  return s.includes(q);
};

export const DefaultCompare = (o1: any, o2: any): number => {
  if (o1.id === '0') {
    return -1;
  }
  if (o2.id === '0') {
    return 1;
  }
  return moment(o1.modifyDateTime).isAfter(o2.modifyDateTime) ? -1 : 1;
};

export const EventSourceCompare = (o1: EventSourceTypes, o2: EventSourceTypes): number => {
  if (o1 && o2) {
    return (o1 === o2 || o1.eventId === o2.eventId) ? 0 :
      (moment(o1.fireDateTime).isAfter(o2.fireDateTime) ? -1 : 1);
  }
  return o1 ? 1 : -1;
};

export const ProductProcessCompare = (o1: ProductProcess, o2: ProductProcess): number => {
  if (o1 && o2) {
    return (o1 === o2 || o1.id === o2.id) ? 0 : o1.sortBy - o2.sortBy;
  }
  return o1 ? 1 : -1;
};

export const LineCompare = (o1: Line, o2: Line): number => {
  if (o1 && o2) {
    return (o1 === o2 || o1.id === o2.id) ? 0 : o1.name.localeCompare(o2.name);
  }
  return o1 ? 1 : -1;
};

export const LineMachineCompare = (o1: LineMachine, o2: LineMachine): number => {
  if (o1 && o2) {
    if ((o1 === o2 || o1.id === o2.id)) {
      return 0;
    }
    const lineCompare = LineCompare(o1.line, o2.line);
    return lineCompare !== 0 ? lineCompare : (o1.item - o2.item);
  }
  return o1 ? 1 : -1;
};

export const filterRouter = (action: RouterNavigationAction, component: ComponentType<any>): boolean => {
  const {routerState} = action.payload;
  let route = routerState.root;
  while (route.firstChild) {
    route = route.firstChild;
  }
  return route.component === component;
};
