import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {from, Subject} from 'rxjs';
import {filter, finalize, map, mergeMap, tap, toArray} from 'rxjs/operators';
import {isArray} from 'util';
import {Line} from '../../models/line';
import {ApiService} from '../../services/api.service';
import {Storage} from '../../services/storage';
import {compareWithId, UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SetLoading, ShowError} from '../../store/actions/core';
import {BatchRangeInputComponentModule} from '../batch-range-input/batch-range-input.component';

const BATCH_MODEL = 'BATCH_MODEL';

@Component({
  templateUrl: './line-update-dialog.component.html',
  styleUrls: ['./line-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineUpdateDialogComponent implements OnInit, OnDestroy {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-line-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly workshops$ = this.apiService.listWorkshop();
  readonly batchModel: boolean;
  dialogTitle: string;
  form: FormGroup;
  private readonly _destroy$ = new Subject();
  private readonly lineKey = 'LineUpdateDialogComponent.line';

  constructor(private store: Store<any>,
              private storage: Storage,
              private fb: FormBuilder,
              private utilService: UtilService,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<LineUpdateDialogComponent, Line | Line[]>,
              @Inject(MAT_DIALOG_DATA)  data: { model: string, line: Line }) {
    this.batchModel = data.model === BATCH_MODEL;
    this.batchModel ? this.batchInit() : this.init(data.line);
  }

  get batchRange() {
    return this.form.get('batchRange');
  }

  get name() {
    return this.form.get('name');
  }

  get workshop() {
    return this.form.get('workshop');
  }

  get doffingType() {
    return this.form.get('doffingType');
  }

  private get preLine(): Line {
    return Line.assign(this.storage.getItem(this.lineKey));
  }

  static open(dialog: MatDialog, data: { line: Line }): MatDialogRef<LineUpdateDialogComponent, Line> {
    return dialog.open(LineUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  static batchCreate(dialog: MatDialog): MatDialogRef<LineUpdateDialogComponent, Line[]> {
    const data = {model: BATCH_MODEL};
    return dialog.open(LineUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  ngOnInit(): void {
    this.dialogRef.beforeClose()
      .pipe(
        map(it => isArray(it) ? it[0] : it),
        filter(it => !!it)
      )
      .subscribe(it => this.storage.setItem(this.lineKey, it));
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  submit() {
    this.apiService.saveLine(this.form.value).subscribe(
      it => this.dialogRef.close(it),
      err => this.store.dispatch(new ShowError(err))
    );
  }

  batchSubmit() {
    const formValue = this.form.value;
    const {batchRange} = formValue;
    delete formValue.batchRange;
    from(batchRange.values)
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        mergeMap(name => {
          const value = {...formValue, name};
          return this.apiService.saveLine(value);
        }),
        toArray(),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  private init(line: Line) {
    this.dialogTitle = line.id ? 'Common.edit' : 'Common.create';
    const {workshop, product, doffingType} = this.preLine;
    this.form = this.fb.group({
      id: line.id,
      name: [line.name, Validators.required],
      workshop: [line.workshop || workshop, Validators.required],
      doffingType: [line.doffingType || doffingType || 'AUTO', Validators.required]
    });
  }

  private batchInit() {
    this.dialogTitle = 'Common.batch';
    const {workshop, product, doffingType} = this.preLine;
    this.form = this.fb.group({
      batchRange: [null, Validators.required],
      workshop: [workshop, Validators.required],
      doffingType: [doffingType || 'AUTO', Validators.required]
    });
  }

}

@NgModule({
  imports: [
    SharedModule,
    BatchRangeInputComponentModule
  ],
  declarations: [
    LineUpdateDialogComponent
  ],
  entryComponents: [
    LineUpdateDialogComponent
  ],
  exports: [
    LineUpdateDialogComponent
  ]
})
export class LineUpdateDialogComponentModule {
}
