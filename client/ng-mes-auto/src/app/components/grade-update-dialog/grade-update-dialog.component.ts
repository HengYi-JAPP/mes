import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {Grade} from '../../models/grade';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './grade-update-dialog.component.html',
  styleUrls: ['./grade-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GradeUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-grade-update-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly form: FormGroup;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<GradeUpdateDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { grade: Grade }) {
    const {grade} = data;
    this.dialogTitle = grade.id ? 'Common.edit' : 'Common.create';
    this.form = fb.group({
      id: grade.id,
      name: [grade.name, Validators.required],
      sortBy: [grade.sortBy, Validators.required]
    });
  }

  get name() {
    return this.form.get('name');
  }

  get sortBy() {
    return this.form.get('sortBy');
  }

  static open(dialog: MatDialog, data: { grade: Grade }): MatDialogRef<GradeUpdateDialogComponent, Grade> {
    return dialog.open(GradeUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveGrade(this.form.value).subscribe(
      it => {
        this.dialogRef.close(it);
      },
      err => {
        this.store.dispatch(new ShowError(err));
      }
    );
  }
}


@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    GradeUpdateDialogComponent
  ],
  entryComponents: [
    GradeUpdateDialogComponent
  ],
  exports: [
    GradeUpdateDialogComponent
  ]
})
export class GradeUpdateDialogComponentModule {
}
