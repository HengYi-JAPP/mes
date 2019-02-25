import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormControl} from '@angular/forms';
import {MatAutocompleteSelectedEvent, MatButtonToggleChange, MatDialog} from '@angular/material';
import {Router} from '@angular/router';
import {Actions, ofType} from '@ngrx/effects';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap, takeUntil, withLatestFrom} from 'rxjs/operators';
import {isString} from 'util';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {CoreActionTypes, SockjsRec} from '../../store/actions/core';
import {EventSourceSockjsRec, SetFireDateTimeSort} from '../../store/actions/silk-runtime-page';
import {
  silkRuntimePageEventSources,
  silkRuntimePageFireDateTimeSortDirection,
  silkRuntimePageSilkCarRecord,
  silkRuntimePageSilkCarRuntime
} from '../../store/silk-car';

@Component({
  templateUrl: './silk-car-runtime-page.component.html',
  styleUrls: ['./silk-car-runtime-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarRuntimePageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-silk-car-runtime-page') b2 = true;
  readonly silkCarRuntime$ = this.store.select(silkRuntimePageSilkCarRuntime);
  readonly silkCarRecord$ = this.store.select(silkRuntimePageSilkCarRecord);
  readonly eventSources$ = this.store.select(silkRuntimePageEventSources);
  readonly fireDateTimeSortDirection$ = this.store.select(silkRuntimePageFireDateTimeSortDirection);
  readonly qCtrl = new FormControl();
  private readonly _destroy$ = new Subject();
  readonly silkCars$ = this.qCtrl.valueChanges
    .pipe(
      takeUntil(this._destroy$),
      debounceTime(SEARCH_DEBOUNCE_TIME),
      distinctUntilChanged(),
      filter(it => it && isString(it) && it.trim().length > 1),
      switchMap(q => this.apiService.autoCompleteSilkCar(q))
    );

  constructor(private store: Store<any>,
              private actions$: Actions,
              private fb: FormBuilder,
              private router: Router,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  onSilkCarSelected(ev: MatAutocompleteSelectedEvent) {
    const {value: {code}} = ev.option;
    this.qCtrl.reset();
    this.router.navigate(['silkCar', 'runtime'], {queryParams: {code}});
  }

  onFireDateTimeSortChange(ev: MatButtonToggleChange) {
    const direction = ev.value;
    this.store.dispatch(new SetFireDateTimeSort({direction}));
  }

  ngOnInit(): void {
    this.actions$
      .pipe(
        takeUntil(this._destroy$),
        ofType<SockjsRec>(CoreActionTypes.SockjsRec),
        withLatestFrom(this.silkCarRecord$),
        filter(([action, silkCarRecord]) => {
          const {body} = action.payload;
          const newId = body.silkCarRecord && body.silkCarRecord.id;
          const oldId = silkCarRecord && silkCarRecord.id;
          return newId === oldId;
        })
      )
      .subscribe(([sockjsRec]) => {
        this.store.dispatch(new EventSourceSockjsRec({sockjsRec}));
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

}
