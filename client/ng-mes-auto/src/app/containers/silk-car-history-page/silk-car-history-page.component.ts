import {ChangeDetectionStrategy, Component, HostBinding, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {Subject} from 'rxjs';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';

@Component({
  templateUrl: './silk-car-history-page.component.html',
  styleUrls: ['./silk-car-history-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkCarHistoryPageComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-silk-car-history-page') b2 = true;
  private _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

}
