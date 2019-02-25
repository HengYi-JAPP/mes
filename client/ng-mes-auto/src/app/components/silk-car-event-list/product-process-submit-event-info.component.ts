import {ChangeDetectionStrategy, Component, Input, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject, forkJoin, Observable, of, Subject} from 'rxjs';
import {finalize, map} from 'rxjs/operators';
import {ProductProcessSubmitEvent} from '../../models/event-source';
import {FormConfig} from '../../models/form-config';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {ShowError} from '../../store/actions/core';

@Component({
  selector: 'app-product-process-submit-event-info',
  templateUrl: './product-process-submit-event-info.component.html',
  styleUrls: ['./product-process-submit-event-info.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductProcessSubmitEventInfoComponent implements OnDestroy {
  readonly stateChange$ = new BehaviorSubject(false);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  get silkRuntimes() {
    return this.event && this.event.silkRuntimes;
  }

  get silkExceptions() {
    return this.event && this.event.silkExceptions;
  }

  get silkNotes() {
    return this.event && this.event.silkNotes;
  }

  get formConfig(): FormConfig {
    return this.event && this.event.formConfig;
  }

  get formConfigValueData(): any {
    return this.event && this.event.formConfigValueData;
  }

  private _event: ProductProcessSubmitEvent;

  @Input()
  get event(): ProductProcessSubmitEvent {
    return this._event;
  }

  set event(event: ProductProcessSubmitEvent) {
    this.fillData(event)
      .pipe(
        finalize(() => this.stateChange$.next(true))
      )
      .subscribe(
        it => this._event = it,
        err => this.store.dispatch(new ShowError(err))
      );
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  private fillData(event: ProductProcessSubmitEvent): Observable<ProductProcessSubmitEvent> {
    if (!event) {
      return of(event);
    }
    const {operator, productProcess, silkExceptions, silkNotes} = event;
    const operator$ = this.apiService.getOperator(operator.id);
    const productProcess$ = this.apiService.getProductProcess(productProcess.id);
    const silkExceptions$ = (silkExceptions && silkExceptions.length > 0) ? forkJoin((silkExceptions.map(it => this.apiService.getSilkException(it.id)))) : of(null);
    const silkNote$ = (silkNotes && silkNotes.length > 0) ? forkJoin((silkNotes.map(it => this.apiService.getSilkNote(it.id)))) : of(null);
    return forkJoin(operator$, productProcess$, silkExceptions$, silkNote$)
      .pipe(map(([it1, it2, it3, it4]) => {
          event.operator = it1;
          event.productProcess = it2;
          event.silkExceptions = it3;
          event.silkNotes = it4;
          return {...event};
        })
      );
  }

}
