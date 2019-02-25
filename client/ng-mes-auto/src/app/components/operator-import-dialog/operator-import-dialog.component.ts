import {HttpParams} from '@angular/common/http';
import {ChangeDetectionStrategy, Component, HostBinding, NgModule} from '@angular/core';
import {FormBuilder, FormControl} from '@angular/forms';
import {MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {BehaviorSubject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, map, switchMap} from 'rxjs/operators';
import {SEARCH_DEBOUNCE_TIME} from '../../../environments/environment';
import {Operator} from '../../models/operator';
import {SuggestOperator} from '../../models/suggest-operator';
import {ApiService} from '../../services/api.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {PermissionInputComponentModule} from '../permission-input/permission-input.component';

class State {
  suggestOperators: SuggestOperator[] = [];
  q: string;
}

const getSuggestOperators = (state: State) => state.suggestOperators;
const getQ = (state: State) => state.q;

@Component({
  templateUrl: './operator-import-dialog.component.html',
  styleUrls: ['./operator-import-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class OperatorImportDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-operator-import-dialog') b2 = true;
  readonly dialogTitle: string;
  readonly qCtrl = new FormControl();
  private readonly state$ = new BehaviorSubject(new State());
  readonly suggestOperators$ = this.state$.pipe(map(getSuggestOperators));

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<OperatorImportDialogComponent>) {
    this.dialogTitle = 'Tooltip.importOperator';
    this.qCtrl.valueChanges
      .pipe(
        debounceTime(SEARCH_DEBOUNCE_TIME),
        distinctUntilChanged(),
        filter(it => !!it),
        map(it => new HttpParams().set('q', it)),
        switchMap(it => this.apiService.listSuggestOperator(it))
      )
      .subscribe(suggestOperators => {
        const next = {...this.state$.value, suggestOperators};
        this.state$.next(next);
      }, error => this.store.dispatch(new ShowError(error)));
  }

  static open(dialog: MatDialog): MatDialogRef<OperatorImportDialogComponent, Operator> {
    return dialog.open(OperatorImportDialogComponent, {panelClass: 'my-dialog'});
  }

  import(operator: any) {
    this.apiService.saveOperator(operator).subscribe(
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
    SharedModule,
    PermissionInputComponentModule
  ],
  declarations: [
    OperatorImportDialogComponent
  ],
  entryComponents: [
    OperatorImportDialogComponent
  ],
  exports: [
    OperatorImportDialogComponent
  ]
})
export class OperatorImportDialogComponentModule {
}
