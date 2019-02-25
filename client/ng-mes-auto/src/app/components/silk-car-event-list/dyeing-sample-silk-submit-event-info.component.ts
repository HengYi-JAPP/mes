import {ChangeDetectionStrategy, Component, Input, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject, forkJoin, Observable, of, Subject} from 'rxjs';
import {finalize, map} from 'rxjs/operators';
import {DyeingSampleSilkSubmitEvent} from '../../models/event-source';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {ShowError} from '../../store/actions/core';

@Component({
  selector: 'app-dyeing-sample-silk-submit-event-info',
  templateUrl: './dyeing-sample-silk-submit-event-info.component.html',
  styleUrls: ['./dyeing-sample-silk-submit-event-info.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DyeingSampleSilkSubmitEventInfoComponent implements OnDestroy {
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

  private _event: DyeingSampleSilkSubmitEvent;

  @Input()
  get event(): DyeingSampleSilkSubmitEvent {
    return this._event;
  }

  set event(event: DyeingSampleSilkSubmitEvent) {
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

  private fillData(event: DyeingSampleSilkSubmitEvent): Observable<DyeingSampleSilkSubmitEvent> {
    if (!event) {
      return of(event);
    }
    const {operator} = event;
    const operator$ = this.apiService.getOperator(operator.id);
    return forkJoin(operator$)
      .pipe(map(([it1, it2, it3, it4]) => {
          event.operator = it1;
          return {...event};
        })
      );
  }

}
