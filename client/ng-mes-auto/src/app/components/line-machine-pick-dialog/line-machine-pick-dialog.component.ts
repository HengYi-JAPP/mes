import {HttpParams} from '@angular/common/http';
import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, OnDestroy, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import {MAT_DIALOG_DATA, MatAutocompleteSelectedEvent, MatDialog, MatDialogRef, MatSelectionList} from '@angular/material';
import {createSelector} from '@ngrx/store';
import {BehaviorSubject, Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map, switchMap, take, takeUntil} from 'rxjs/operators';
import {isString} from 'util';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {Line} from '../../models/line';
import {LineMachine} from '../../models/line-machine';
import {ApiService} from '../../services/api.service';
import {LineCompare, LineMachineCompare} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {
  LineMachineUpdateDialogComponent,
  LineMachineUpdateDialogComponentModule
} from '../line-machine-update-dialog/line-machine-update-dialog.component';

class State {
  sourceEntities: { [id: string]: LineMachine } = {};
  destEntities: { [id: string]: LineMachine } = {};
}

const getSourceEntities = (state: State) => state.sourceEntities;
const getDestEntities = (state: State) => state.destEntities;
const getDest = createSelector(getDestEntities, entities =>
  Object.values(entities).sort(LineMachineCompare)
);
const getSource = createSelector(getSourceEntities, entities =>
  Object.values(entities).sort(LineMachineCompare)
);

@Component({
  templateUrl: './line-machine-pick-dialog.component.html',
  styleUrls: ['./line-machine-pick-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineMachinePickDialogComponent implements OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.line-machine-pick-dialog') b2 = true;
  @ViewChild(MatSelectionList) selectionList: MatSelectionList;
  readonly lineCtrl = new FormControl();
  private readonly _destroy$ = new Subject();
  readonly lines$ = this.lineCtrl.valueChanges.pipe(
    takeUntil(this._destroy$),
    debounceTime(SEARCH_DEBOUNCE_TIME),
    distinctUntilChanged(),
    filter(it => it && isString(it) && it.trim().length > 0),
    switchMap(q => {
      const params = new HttpParams().set('pageSize', '10').set('q', q);
      return this.apiService.listLine(params);
    }),
    map(({lines}) => lines.sort(LineCompare))
  );
  private readonly state$ = new BehaviorSubject(new State());
  readonly source$ = this.state$.pipe(map(getSource));
  readonly dest$ = this.state$.pipe(map(getDest));

  constructor(private dialog: MatDialog,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<LineMachinePickDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { dest: LineMachine[] }) {
    const destEntities = LineMachine.toEntities(data.dest);
    const next = {...this.state$.value, destEntities};
    this.state$.next(next);
  }

  static open(dialog: MatDialog, data: { dest: LineMachine[] }): MatDialogRef<LineMachinePickDialogComponent, LineMachine[]> {
    return dialog.open(LineMachinePickDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  onLineSelected(ev: MatAutocompleteSelectedEvent) {
    const line: Line = ev.option.value;
    this.lineCtrl.reset();
    this.apiService.getLine_LineMachines(line.id)
      .subscribe(it => this.refreshSource(it));
  }

  toDestAll() {
    const {sourceEntities} = this.state$.value;
    const destEntities = {...this.state$.value.destEntities};
    Object.keys(sourceEntities).forEach(id => destEntities[id] = sourceEntities[id]);
    const next = {...this.state$.value, destEntities, sourceEntities: {}};
    this.state$.next(next);
  }

  toDest(value: LineMachine) {
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

  toSource(value: LineMachine) {
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

  create() {
    const lineMachine = LineMachine.assign();
    LineMachineUpdateDialogComponent.open(this.dialog, {lineMachine})
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

  private refreshSource(source: LineMachine[]) {
    const sourceEntities = LineMachine.toEntities(source);
    const destEntities = {...this.state$.value.destEntities};
    Object.keys(destEntities).forEach(it => delete sourceEntities[it]);
    const next = {...this.state$.value, sourceEntities};
    this.state$.next(next);
  }
}


@NgModule({
  imports: [
    SharedModule,
    LineMachineUpdateDialogComponentModule
  ],
  declarations: [
    LineMachinePickDialogComponent
  ],
  entryComponents: [
    LineMachinePickDialogComponent
  ],
  exports: [
    LineMachinePickDialogComponent
  ]
})
export class LineMachinePickDialogComponentModule {
}
