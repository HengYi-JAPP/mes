import {ChangeDetectionStrategy, Component, HostBinding, Inject, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef, MatSelectionList} from '@angular/material';
import {createSelector} from '@ngrx/store';
import {BehaviorSubject, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map, take, takeUntil} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {SilkNote} from '../../models/silk-note';
import {ApiService} from '../../services/api.service';
import {CheckQ} from '../../services/util.service';
import {SilkNoteUpdateDialogComponent} from './silk-note-update-dialog.component';

class State {
  sourceEntities: { [id: string]: SilkNote } = {};
  q: string;
  destEntities: { [id: string]: SilkNote } = {};
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
  templateUrl: './silk-note-pick-dialog.component.html',
  styleUrls: ['./silk-note-pick-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkNotePickDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-silk-note-pick-dialog') b2 = true;
  @ViewChild(MatSelectionList) selectionList: MatSelectionList;
  readonly qCtrl = new FormControl();
  private readonly state$ = new BehaviorSubject(new State());
  readonly source$ = this.state$.pipe(map(getSource));
  readonly dest$ = this.state$.pipe(map(getDest));
  private readonly _destroy$ = new Subject();

  constructor(private dialog: MatDialog,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<SilkNotePickDialogComponent, SilkNote[]>,
              @Inject(MAT_DIALOG_DATA)  data: { silkNotes: SilkNote[] }) {
    const destEntities = SilkNote.toEntities(data.silkNotes);
    this.apiService.listSilkNote()
      .subscribe(allSilkNotes => {
        const sourceEntities = SilkNote.toEntities(allSilkNotes);
        Object.keys(destEntities).forEach(it => delete sourceEntities[it]);
        const next = {...this.state$.value, destEntities, sourceEntities};
        this.state$.next(next);
      });
  }

  static open(dialog: MatDialog, data: { silkNotes: SilkNote[] }): MatDialogRef<SilkNotePickDialogComponent, SilkNote[]> {
    return dialog.open(SilkNotePickDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
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
    const silkNote = SilkNote.assign();
    SilkNoteUpdateDialogComponent.open(this.dialog, {silkNote})
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

  toDest(value: SilkNote) {
    const sourceEntities = {...this.state$.value.sourceEntities};
    const destEntities = {...this.state$.value.destEntities};
    delete sourceEntities[value.id];
    destEntities[value.id] = value;
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

  toSource(value: SilkNote) {
    const sourceEntities = {...this.state$.value.sourceEntities};
    const destEntities = {...this.state$.value.destEntities};
    delete destEntities[value.id];
    sourceEntities[value.id] = value;
    const next = {...this.state$.value, destEntities, sourceEntities};
    this.state$.next(next);
  }

  submit() {
    this.dest$.pipe(take(1))
      .subscribe(it => this.dialogRef.close(it));
  }
}
