import {ChangeDetectionStrategy, Component, ElementRef, HostBinding, Input, NgModule, OnDestroy} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject, Subject} from 'rxjs';
import {EventSourceTypes} from '../../models/event-source';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {FormConfigValueModule} from '../form-config-value/form-config-value-input.component';
import {DyeingSampleSilkSubmitEventInfoComponent} from './dyeing-sample-silk-submit-event-info.component';
import {ProductProcessSubmitEventInfoComponent} from './product-process-submit-event-info.component';
import {SilkRuntimeDetachEventInfoComponent} from './silk-runtime-detach-event-info.component';

@Component({
  selector: 'app-silk-car-event-list',
  templateUrl: './silk-car-event-list.component.html',
  styleUrls: ['./silk-car-event-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarEventListComponent implements OnDestroy {
  @HostBinding('class.app-comp') b1 = true;
  @HostBinding('class.app-silk-car-event-list') b2 = true;
  readonly eventSources$ = new BehaviorSubject([]);
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private elRef: ElementRef,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  @Input()
  set eventSources(eventSources: EventSourceTypes[]) {
    this.eventSources$.next(eventSources);
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }
}

@NgModule({
  imports: [
    SharedModule,
    FormConfigValueModule
  ],
  declarations: [
    SilkCarEventListComponent,
    SilkRuntimeDetachEventInfoComponent,
    DyeingSampleSilkSubmitEventInfoComponent,
    ProductProcessSubmitEventInfoComponent
  ],
  exports: [
    SilkCarEventListComponent,
    SilkRuntimeDetachEventInfoComponent,
    DyeingSampleSilkSubmitEventInfoComponent,
    ProductProcessSubmitEventInfoComponent
  ]
})
export class SilkCarEventListComponentModule {
}
