import {coerceBooleanProperty, coerceNumberProperty} from '@angular/cdk/coercion';
import {ChangeDetectionStrategy, Component, forwardRef, Input, NgModule} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject} from 'rxjs';
import {filter} from 'rxjs/operators';
import {SilkNote} from '../../models/silk-note';
import {UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {SortDialogComponent, SortDialogComponentModule} from '../sort-dialog/sort-dialog.component';
import {SilkNotePickDialogComponent} from './silk-note-pick-dialog.component';
import {SilkNoteUpdateDialogComponent} from './silk-note-update-dialog.component';

@Component({
  selector: 'app-silk-note-input',
  templateUrl: './silk-note-input.component.html',
  styleUrls: ['./silk-note-input.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => SilkNoteInputComponent),
    multi: true
  }]
})
export class SilkNoteInputComponent implements ControlValueAccessor {
  readonly silkNotes$ = new BehaviorSubject<SilkNote[]>([]);
  onModelChange: Function;
  onModelTouched: Function;

  constructor(private store: Store<any>,
              private dialog: MatDialog,
              private utilService: UtilService) {
  }

  private _required = false;

  @Input()
  get required() {
    return this._required;
  }

  set required(req) {
    this._required = coerceBooleanProperty(req);
  }

  private _minLength: number;

  @Input()
  get minLength(): number {
    return this._minLength;
  }

  set minLength(min: number) {
    this._minLength = coerceNumberProperty(min);
  }

  sort() {
    SortDialogComponent.open<SilkNote>(this.dialog, {datas: this.silkNotes$.value, displayKeys: 'name'})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  pick() {
    SilkNotePickDialogComponent.open(this.dialog, {silkNotes: this.silkNotes$.value})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.handleChange(it));
  }

  update(silkNote: SilkNote) {
    SilkNoteUpdateDialogComponent.open(this.dialog, {silkNote})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(newSilkNote => {
        const silkNotes = this.silkNotes$.value
          .map(it => it.id === newSilkNote.id ? newSilkNote : it);
        this.handleChange(silkNotes);
        this.utilService.showSuccess();
      });
  }

  handleChange(silkNotes: SilkNote[] = []): void {
    this.silkNotes$.next(silkNotes);
    this.onModelChange(silkNotes);
  }

  writeValue(value: SilkNote[] = []): void {
    this.silkNotes$.next(value);
  }

  setDisabledState(isDisabled: boolean): void {
  }

  registerOnChange(fn: any): void {
    this.onModelChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onModelTouched = fn;
  }

}

@NgModule({
  imports: [
    SharedModule,
    SortDialogComponentModule
  ],
  declarations: [
    SilkNoteInputComponent,
    SilkNotePickDialogComponent,
    SilkNoteUpdateDialogComponent
  ],
  entryComponents: [
    SilkNotePickDialogComponent,
    SilkNoteUpdateDialogComponent
  ],
  exports: [
    SilkNoteInputComponent,
    SilkNotePickDialogComponent,
    SilkNoteUpdateDialogComponent
  ]
})
export class SilkNoteModule {
}
