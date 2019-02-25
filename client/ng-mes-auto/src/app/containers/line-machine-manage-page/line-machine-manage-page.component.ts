import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog, MatSelectChange} from '@angular/material';
import {ActivatedRoute, Router} from '@angular/router';
import {Store} from '@ngrx/store';
import {of} from 'rxjs';
import {catchError, filter, finalize, map, switchMap, take, tap, withLatestFrom} from 'rxjs/operators';
import {LineMachineUpdateDialogComponent} from '../../components/line-machine-update-dialog/line-machine-update-dialog.component';
import {LineMachine} from '../../models/line-machine';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {SetLoading, ShowError} from '../../store/actions/core';
import {DeleteSuccess, SaveSuccess} from '../../store/actions/line-machine-manage-page';
import {lineMachineManagePageLine, lineMachineManagePageLineMachines, lineMachineManagePageLines} from '../../store/config';

@Component({
  templateUrl: './line-machine-manage-page.component.html',
  styleUrls: ['./line-machine-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LineMachineManagePageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-line-machine-manage-page') b2 = true;
  readonly displayedColumns = ['id', 'line', 'item', 'spindleNum', 'btns'];
  readonly lines$ = this.store.select(lineMachineManagePageLines);
  readonly line$ = this.store.select(lineMachineManagePageLine);
  readonly lineMachines$ = this.store.select(lineMachineManagePageLineMachines);

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private router: Router,
              private route: ActivatedRoute,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  lineChange(ev: MatSelectChange) {
    const queryParams = {lineId: ev.value};
    this.router.navigate(['config/lineMachines'], {queryParams});
  }

  create() {
    this.line$.pipe(
      take(1),
      tap(line => {
        const lineMachine = LineMachine.assign({line});
        this.update(lineMachine);
      })
    ).subscribe();
  }

  createBatch() {
    LineMachineUpdateDialogComponent.batchCreate(this.dialog)
      .afterClosed()
      .pipe(
        filter(it => !!it),
        withLatestFrom(this.line$)
      )
      .subscribe(([lineMachines, line]) => {
        const lineId = lineMachines[0].line.id;
        if (lineId === line.id) {
          lineMachines.forEach(lineMachine => {
            this.store.dispatch(new SaveSuccess({lineMachine}));
          });
        } else {
          this.router.navigate(['config', 'lineMachines'], {queryParams: {lineId}});
        }
        this.utilService.showSuccess();
      });
  }

  update(lineMachine: LineMachine) {
    LineMachineUpdateDialogComponent.open(this.dialog, {lineMachine})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.utilService.showSuccess();
        this.store.dispatch(new SaveSuccess({lineMachine: it}));
      });
  }

  delete(lineMachine: LineMachine) {
    this.utilService.showConfirm()
      .pipe(
        tap(() => this.store.dispatch(new SetLoading())),
        switchMap(() => this.apiService.deleteLineMachine(lineMachine.id)),
        map(() => new DeleteSuccess({id: lineMachine.id})),
        tap(() => this.utilService.showSuccess()),
        catchError(error => of(new ShowError(error))),
        finalize(() => this.store.dispatch(new SetLoading(false)))
      )
      .subscribe(it => this.store.dispatch(it));
  }

}
