import {ChangeDetectionStrategy, Component, HostBinding, Inject} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {SilkNote} from '../../models/silk-note';
import {ApiService} from '../../services/api.service';
import {compareWithId} from '../../services/util.service';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './silk-note-update-dialog.component.html',
  styleUrls: ['./silk-note-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SilkNoteUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-silk-note-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly dialogTitle = this.silkNote.id ? 'Common.edit' : 'Common.create';
  readonly form = this.fb.group({
    id: this.silkNote.id,
    name: [this.silkNote.name, Validators.required]
  });

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<SilkNoteUpdateDialogComponent, SilkNote>,
              @Inject(MAT_DIALOG_DATA) private data: { silkNote: SilkNote }) {
  }

  get silkNote() {
    return this.data.silkNote;
  }

  get name() {
    return this.form.get('name');
  }

  static open(dialog: MatDialog, data: { silkNote: SilkNote }): MatDialogRef<SilkNoteUpdateDialogComponent, SilkNote> {
    return dialog.open(SilkNoteUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveSilkNote(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}
