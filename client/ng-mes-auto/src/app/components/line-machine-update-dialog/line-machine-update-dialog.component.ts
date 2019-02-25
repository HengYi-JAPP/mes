import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {from, Observable, Subject} from 'rxjs';
import {filter, map, switchMap, takeUntil, toArray} from 'rxjs/operators';
import {isArray} from 'util';
import {Line} from '../../models/line';
import {LineMachine} from '../../models/line-machine';
import {ApiService} from '../../services/api.service';
import {Storage} from '../../services/storage';
import {compareWithId} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {BatchRangeInputComponentModule} from '../batch-range-input/batch-range-input.component';
import {SortDialogComponent, SortDialogComponentModule} from '../sort-dialog/sort-dialog.component';

const BATCH_MODEL = 'BATCH_MODEL';

@Component({
  templateUrl: './line-machine-update-dialog.component.html',
  styleUrls: ['./line-machine-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineMachineUpdateDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-line-machine-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly _destory$ = new Subject();
  readonly batchModel: boolean;
  readonly lines$: Observable<Line[]> = this.apiService.listLine().pipe(map(({lines}) => lines));
  dialogTitle: string;
  form: FormGroup;
  private readonly lineMachineKey = `${LineMachineUpdateDialogComponent.name}.lineMachine`;

  constructor(private store: Store<any>,
              private storage: Storage,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<LineMachineUpdateDialogComponent, LineMachine | LineMachine[]>,
              @Inject(MAT_DIALOG_DATA)  data: { model: string, lineMachine: LineMachine }) {
    this.batchModel = data.model === BATCH_MODEL;
    this.batchModel ? this.batchInit() : this.init(data.lineMachine);
  }

  get spindleSeqCtrl() {
    return this.form.get('spindleSeq');
  }

  get batchRange() {
    return this.form.get('batchRange');
  }

  get line() {
    return this.form.get('line');
  }

  get item() {
    return this.form.get('item');
  }

  get spindleNumCtrl() {
    return this.form.get('spindleNum');
  }

  private get preLineMachine(): LineMachine {
    return LineMachine.assign(this.storage.getItem(this.lineMachineKey));
  }

  static open(dialog: MatDialog, data: { lineMachine: LineMachine }): MatDialogRef<LineMachineUpdateDialogComponent, LineMachine> {
    return dialog.open(LineMachineUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  static batchCreate(dialog: MatDialog): MatDialogRef<LineMachineUpdateDialogComponent, LineMachine[]> {
    const data = {model: BATCH_MODEL};
    return dialog.open(LineMachineUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  sortSpindleSeq(): void {
    const spindleNum = this.spindleNumCtrl.value;
    let value = this.spindleSeqCtrl.value || [];
    if (value.length !== spindleNum) {
      value = [];
      for (let i = 1; i <= spindleNum; i++) {
        value.push(i);
      }
    }
    SortDialogComponent.open<number>(this.dialog, {datas: value, displayKeys: null})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.spindleSeqCtrl.setValue([...it]);
        this.spindleSeqCtrl.markAsDirty();
      });
  }

  ngOnInit(): void {
    this.dialogRef.beforeClose()
      .pipe(
        map(it => isArray(it) ? it[0] : it),
        filter(it => !!it)
      )
      .subscribe(it => this.storage.setItem(this.lineMachineKey, it));
    this.spindleNumCtrl.valueChanges
      .pipe(
        takeUntil(this._destory$)
      )
      .subscribe(() => this.spindleSeqCtrl.setValue(null));
  }

  submit() {
    this.apiService.saveLineMachine(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  batchSubmit() {
    const formValue = this.form.value;
    const {batchRange} = formValue;
    delete formValue.batchRange;
    from<string>(batchRange.values)
      .pipe(
        map(it => {
          const item = parseInt(it, 10);
          return {...formValue, item};
        }),
        toArray(),
        switchMap(it => this.apiService.batchLineMachines(it))
      )
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  ngOnDestroy(): void {
    this._destory$.next();
    this._destory$.complete();
  }

  private init(lineMachine: LineMachine) {
    this.dialogTitle = lineMachine.id ? 'Common.edit' : 'Common.create';
    const {line, spindleNum} = this.preLineMachine;
    this.form = this.fb.group({
      id: lineMachine.id,
      line: [lineMachine.line || line, Validators.required],
      item: [lineMachine.item, [Validators.required, Validators.min(1)]],
      spindleNum: [lineMachine.spindleNum || spindleNum, [Validators.required, Validators.min(1)]],
      spindleSeq: [lineMachine.spindleSeq, [Validators.required, Validators.minLength(1)]]
    });
  }

  private batchInit() {
    this.dialogTitle = 'Common.batch';
    const {line, spindleNum, spindleSeq} = this.preLineMachine;
    this.form = this.fb.group({
      batchRange: [null, Validators.required],
      line: [line, Validators.required],
      spindleNum: [spindleNum, [Validators.required, Validators.min(1)]],
      spindleSeq: [spindleSeq, [Validators.required, Validators.minLength(1)]]
    });
  }

}


@NgModule({
  imports: [
    SharedModule,
    BatchRangeInputComponentModule,
    SortDialogComponentModule
  ],
  declarations: [
    LineMachineUpdateDialogComponent
  ],
  entryComponents: [
    LineMachineUpdateDialogComponent
  ],
  exports: [
    LineMachineUpdateDialogComponent
  ]
})
export class LineMachineUpdateDialogComponentModule {
}
