import {ChangeDetectionStrategy, Component, EventEmitter, Input, NgModule, OnDestroy, Output, ViewChild} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog, MatVerticalStepper} from '@angular/material';
import {createSelector, Store} from '@ngrx/store';
import {BehaviorSubject, forkJoin, Subject} from 'rxjs';
import {filter, finalize, map, switchMap, take, tap, withLatestFrom} from 'rxjs/operators';
import {isNullOrUndefined} from 'util';
import {ROLES} from '../../models/permission';
import {Product} from '../../models/product';
import {ProductProcess} from '../../models/product-process';
import {ApiService} from '../../services/api.service';
import {ProductProcessCompare, UtilService} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';
import {FormConfigModule} from '../form-config/form-config-input.component';
import {
  ProductProcessUpdateDialogComponent,
  ProductProcessUpdateDialogComponentModule
} from '../product-process-update-dialog/product-process-update-dialog.component';
import {SilkExceptionModule} from '../silk-exception/silk-exception-input.component';
import {SilkNoteModule} from '../silk-note/silk-note-input.component';
import {SortDialogComponent, SortDialogComponentModule} from '../sort-dialog/sort-dialog.component';

class State {
  product: Product;
  processFormEntities: { [id: string]: { process: ProductProcess, form: FormGroup } } = {};
}

const getProduct = (state: State) => state.product;
const getProcessFormEntities = (state: State) => state.processFormEntities;
const getProcesses = createSelector(getProcessFormEntities, entities =>
  Object.values(entities)
    .map(it => it.process)
    .sort(ProductProcessCompare)
);
const getProcessForms = createSelector(getProcessFormEntities, getProcesses, (entities, processes) =>
  processes.map(it => entities[it.id].form)
);

@Component({
  selector: 'app-product-process-list',
  templateUrl: './product-process-list.component.html',
  styleUrls: ['./product-process-list.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductProcessListComponent implements OnDestroy {
  readonly ROLES = ROLES;
  @ViewChild(MatVerticalStepper) stepper: MatVerticalStepper;
  @Output()
  readonly onChange = new EventEmitter<ProductProcess>();
  private readonly state$ = new BehaviorSubject(new State());
  readonly product$ = this.state$.pipe(map(getProduct));
  readonly processes$ = this.state$.pipe(map(getProcesses));
  readonly processForms$ = this.state$.pipe(map(getProcessForms));
  private readonly _destroy$ = new Subject();

  constructor(private store: Store<any>,
              private fb: FormBuilder,
              private dialog: MatDialog,
              private utilService: UtilService,
              private apiService: ApiService) {
  }

  @Input()
  set product(product: Product) {
    if (product && product.id) {
      this.apiService.getProduct_ProductProcess(product.id)
        .pipe(
          finalize(() => {
            if (!isNullOrUndefined(this.stepper)) {
              this.stepper.reset();
            }
          })
        )
        .subscribe(
          it => this.refresh(product, it),
          err => this.store.dispatch(new ShowError(err))
        );
    } else {
      const next = {product, processFormEntities: {}};
      this.state$.next(next);
    }
  }

  ngOnDestroy(): void {
    this._destroy$.next();
    this._destroy$.complete();
  }

  deleteProcessForm(form: FormGroup) {
    this.utilService.showConfirm()
      .pipe(
        filter(it => !!it)
      )
      .subscribe();
  }

  submitProcessForm(form: FormGroup) {
    this.apiService.saveProductProcess(form.value)
      .pipe(
        tap(it => this.updateProcess(it))
      )
      .subscribe(
        () => this.utilService.showSuccess(),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  sort() {
    this.processes$
      .pipe(
        take(1),
        switchMap(it => SortDialogComponent.open<ProductProcess>(this.dialog, {datas: it, displayKeys: 'name'}).afterClosed()),
        filter(it => !!it),
        switchMap(processes => {
          let sortBy = 0;
          const {processFormEntities} = this.state$.value;
          const processes$ = processes.map(process => {
            const {form} = processFormEntities[process.id];
            const {value} = form;
            sortBy += 1000;
            value.sortBy = sortBy;
            return this.apiService.saveProductProcess(value);
          });
          return forkJoin(processes$);
        }),
        withLatestFrom(this.product$),
        tap(([processes, product]) => this.refresh(product, processes))
      )
      .subscribe(
        () => this.utilService.showSuccess(),
        err => this.store.dispatch(new ShowError(err))
      );
  }

  create() {
    const process = ProductProcess.assign({product: this.state$.value.product});
    ProductProcessUpdateDialogComponent.open(this.dialog, {process})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.updateProcess(it));
  }

  private refresh(product: Product, processes: ProductProcess[]) {
    const processFormEntities = processes.reduce((acc, process) => {
      const form = this.getProcessForm(process);
      acc[process.id] = {process, form};
      return acc;
    }, {});
    const next = {product, processFormEntities};
    this.state$.next(next);
  }

  private getProcessForm(process: ProductProcess): FormGroup {
    return this.fb.group({
      id: process.id,
      product: [process.product, Validators.required],
      name: [process.name, Validators.required],
      sortBy: [process.sortBy, [Validators.required, Validators.min(0)]],
      formConfig: [process.formConfig],
      exceptions: [process.exceptions, [Validators.required, Validators.min(1)]],
      notes: [process.notes],
      relateRoles: [process.relateRoles]
    });
  }

  private updateProcess(process: ProductProcess) {
    const processFormEntities = {...this.state$.value.processFormEntities};
    const form = this.getProcessForm(process);
    processFormEntities[process.id] = {process, form};
    const next = {...this.state$.value, processFormEntities};
    this.state$.next(next);
  }

}

@NgModule({
  imports: [
    SharedModule,
    FormConfigModule,
    SortDialogComponentModule,
    ProductProcessUpdateDialogComponentModule,
    SilkExceptionModule,
    SilkNoteModule
  ],
  declarations: [
    ProductProcessListComponent
  ],
  exports: [
    ProductProcessListComponent
  ]
})
export class ProductProcessListComponentModule {
}
