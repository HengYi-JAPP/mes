import {ChangeDetectionStrategy, Component, HostBinding, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {Observable, of} from 'rxjs';
import {catchError, filter, finalize, map, switchMap, tap} from 'rxjs/operators';
import {GradeUpdateDialogComponent} from '../../components/grade-update-dialog/grade-update-dialog.component';
import {Grade} from '../../models/grade';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../../store/actions/core';
import {DeleteSuccess, SaveSuccess} from '../../store/actions/grade-manage-page';
import {gradeManagePageGrades} from '../../store/config';

@Component({
  templateUrl: './grade-manage-page.component.html',
  styleUrls: ['./grade-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class GradeManagePageComponent implements OnInit {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-grade-manage-page') b2 = true;
  readonly displayedColumns = ['name', 'sortBy', 'btns'];
  readonly searchForm: FormGroup;
  readonly grades$: Observable<Grade[]>;

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
    this.grades$ = this.store.select(gradeManagePageGrades);
  }

  ngOnInit(): void {
  }

  create() {
    this.update(new Grade());
  }

  update(grade: Grade) {
    GradeUpdateDialogComponent.open(this.dialog, {grade}).afterClosed()
      .pipe(
        filter(it => !!it)
      ).subscribe(
      it => {
        this.utilService.showSuccess();
        this.store.dispatch(new SaveSuccess({grade: it}));
      }
    );
  }

  delete(grade: Grade) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deleteGrade(grade.id)),
        map(() => new DeleteSuccess({id: grade.id})),
        tap(() => this.utilService.showSuccess()),
        catchError(error => of(new ShowError(error))),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(it => this.store.dispatch(it));
  }

}
