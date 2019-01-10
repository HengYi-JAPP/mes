import {ChangeDetectionStrategy, Component, HostBinding, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSelectionList} from '@angular/material';
import {createSelector} from '@ngrx/store';
import {BehaviorSubject, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map, take, takeUntil} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {SilkException} from '../../models/silk-exception';
import {ApiService} from '../../services/api.service';
import {CheckQ} from '../../services/util.service';
import {SilkExceptionUpdateDialogComponent} from './silk-exception-update-dialog.component';

class State {
  sourceEntities: { [id: string]: SilkException } = {};
  q: string;
  destEntities: { [id: string]: SilkException } = {};
}

const getSourceEntities = (state: State) => state.sourceEntities;
const getDestEntities = (state: State) => state.destEntities;
const getQ = (state: State) => state.q;
const getDest = createSelector(getDestEntities, entities => Object.values(entities));
const getSource = createSelector(getSourceEntities, getQ, (entities, q) =>
  Object.values(entities)
    .filter(it => CheckQ(it.name, q))
);

@Component({
  templateUrl: './silk-exception-pick-dialog.component.html',
  styleUrls: ['./silk-exception-pick-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkExceptionPickDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-silk-exception-pick-dialog') b2 = true;
  @ViewChild(MatSelectionList) selectionList: MatSelectionList;
  readonly qCtrl = new FormControl();
  private readonly state$ = new BehaviorSubject(new State());
  readonly source$ = this.state$.pipe(map(getSource));
  readonly dest$ = this.state$.pipe(map(getDest));
  private readonly _destroy$ = new Subject();

  constructor(private dialog: MatDialog,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<SilkExceptionPickDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { silkExceptions: SilkException[] }) {
    const destEntities = SilkException.toEntities(data.silkExceptions);
    this.apiService.listSilkException()
      .subscribe(allSilkExceptions => {
        const sourceEntities = SilkException.toEntities(allSilkExceptions);
        Object.keys(destEntities).forEach(it => delete sourceEntities[it]);
        const next = {...this.state$.value, destEntities, sourceEntities};
        this.state$.next(next);
      });
  }

  static open(dialog: MatDialog, data: { silkExceptions: SilkException[] }): MatDialogRef<SilkExceptionPickDialogComponent, SilkException[]> {
    return dialog.open(SilkExceptionPickDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  ngOnInit(): void {
    this.qCtrl.valueChanges
      .pipe(
        takeUntil(this._destroy$),
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged()
      )
      .subscribe(q => {
        const next = {...this.state$.value, q};
        this.state$.next(next);
      });
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  create() {
    const silkException = SilkException.assign();
    SilkExceptionUpdateDialogComponent.open(this.dialog, {silkException})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        const destEntities = {...this.state$.value.destEntities};
        destEntities[it.id] = it;
        const next = {...this.state$.value, destEntities};
        this.state$.next(next);
      });
  }

  toDestAll() {
    const {sourceEntities} = this.state$.value;
    const destEntities = {...this.state$.value.destEntities};
    Object.keys(sourceEntities).forEach(id => destEntities[id] = sourceEntities[id]);
    const next = {...this.state$.value, destEntities, sourceEntities: {}};
    this.state$.next(next);
  }

  toDest(silkException: SilkException) {
    const sourceEntities = {...this.state$.value.sourceEntities};
    const destEntities = {...this.state$.value.destEntities};
    delete sourceEntities[silkException.id];
    destEntities[silkException.id] = silkException;
    const next = {...this.state$.value, destEntities, sourceEntities};
    this.state$.next(next);
  }

  toSourceAll() {
    const sourceEntities = {...this.state$.value.sourceEntities};
    const {destEntities} = this.state$.value;
    Object.keys(destEntities).forEach(id => sourceEntities[id] = destEntities[id]);
    const next = {...this.state$.value, destEntities: {}, sourceEntities};
    this.state$.next(next);
  }

  toSource(silkException: SilkException) {
    const sourceEntities = {...this.state$.value.sourceEntities};
    const destEntities = {...this.state$.value.destEntities};
    delete destEntities[silkException.id];
    sourceEntities[silkException.id] = silkException;
    const next = {...this.state$.value, destEntities, sourceEntities};
    this.state$.next(next);
  }

  submit() {
    this.dest$.pipe(take(1))
      .subscribe(it => this.dialogRef.close(it));
  }
}
