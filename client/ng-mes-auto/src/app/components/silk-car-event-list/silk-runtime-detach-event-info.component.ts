import {ChangeDetectionStrategy, Component, HostBinding, Input, OnDestroy} from '@angular/core';
import {Store} from '@ngrx/store';
import {BehaviorSubject, forkJoin, Observable, of, Subject} from 'rxjs';
import {finalize, map} from 'rxjs/operators';
import {SilkRuntimeDetachEvent} from '../../models/event-source';
import {ApiService} from '../../services/api.service';
import {ShowError} from '../../store/actions/core';

@Component({
  selector: 'app-silk-runtime-detach-event-info',
  templateUrl: './silk-runtime-detach-event-info.component.html',
  styleUrls: ['./silk-runtime-detach-event-info.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkRuntimeDetachEventInfoComponent implements OnDestroy {
  @HostBinding('class.app-comp') b1 = true;
  @HostBinding('class.app-silk-runtime-detach-event-info') b2 = true;
  readonly stateChange$ = new BehaviorSubject(false);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private apiService: ApiService) {
  }

  get silkRuntimes() {
    return this.event && this.event.silkRuntimes;
  }

  private _event: SilkRuntimeDetachEvent;

  @Input()
  get event(): SilkRuntimeDetachEvent {
    return this._event;
  }

  set event(event: SilkRuntimeDetachEvent) {
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

  private fillData(event: SilkRuntimeDetachEvent): Observable<SilkRuntimeDetachEvent> {
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
