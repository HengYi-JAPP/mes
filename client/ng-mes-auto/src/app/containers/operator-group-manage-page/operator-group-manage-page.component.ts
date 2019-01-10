import {ChangeDetectionStrategy, Component, HostBinding, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {debounceTime, distinctUntilChanged, filter, map, switchMap} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {OperatorGroupUpdateDialogComponent} from '../../components/operator-group-update-dialog/operator-group-update-dialog.component';
import {OperatorGroup} from '../../models/operator-group';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {ShowError} from '../../store/actions/core';
import {SaveSuccess, SetQ} from '../../store/actions/operator-group-manage-page';
import {operatorGroupManagePageOperatorGroups} from '../../store/admin';

@Component({
  templateUrl: './operator-group-manage-page.component.html',
  styleUrls: ['./operator-group-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperatorGroupManagePageComponent implements OnInit {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-operator-group-manage-page') b2 = true;
  readonly displayedColumns = ['name', 'btns'];
  readonly searchForm = this.fb.group({'q': ''});
  readonly operatorGroups$ = this.store.select(operatorGroupManagePageOperatorGroups);

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  get q() {
    return this.searchForm.get('q');
  }

  ngOnInit(): void {
    this.q.valueChanges
      .pipe(
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged(),
        map(q => new SetQ({q}))
      )
      .subscribe(it => this.store.dispatch(it));
  }

  create() {
    this.update(new OperatorGroup());
  }

  update(operatorGroup: OperatorGroup) {
    OperatorGroupUpdateDialogComponent.open(this.dialog, {operatorGroup})
      .afterClosed()
      .pipe(
        filter(it => !!it),
        switchMap(it => this.apiService.saveOperatorGroup(it))
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({operatorGroup: it}));
        this.utilService.showSuccess();
      }, err => {
        this.store.dispatch(new ShowError(err));
      });
  }

}
