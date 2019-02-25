import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {Store} from '@ngrx/store';
import {filter} from 'rxjs/operators';
import {Workshop} from '../../models/workshop';
import {ApiService} from '../../services/api.service';
import {Storage} from '../../services/storage';
import {compareWithId} from '../../services/util.service';
import {SharedModule} from '../../shared.module';
import {ShowError} from '../../store/actions/core';

@Component({
  templateUrl: './workshop-update-dialog.component.html',
  styleUrls: ['./workshop-update-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkshopUpdateDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-workshop-update-dialog') b2 = true;
  readonly compareWithId = compareWithId;
  readonly dialogTitle: string;
  readonly form: FormGroup;
  private readonly workshopKey = 'WorkshopUpdateDialogComponent.workshop';

  constructor(private store: Store<any>,
              private storage: Storage,
              private fb: FormBuilder,
              private apiService: ApiService,
              private dialogRef: MatDialogRef<WorkshopUpdateDialogComponent, Workshop>,
              @Inject(MAT_DIALOG_DATA)  data: { workshop: Workshop }) {
    const workshop = (data.workshop && data.workshop.id) ? data.workshop : this.preWorkshop;
    this.dialogTitle = workshop.id ? 'Common.edit' : 'Common.create';
    this.form = fb.group({
      id: workshop.id,
      name: [workshop.name, Validators.required],
      note: workshop.note,
      corporation: [workshop.corporation, Validators.required]
    });
    if (!workshop.corporation) {
      this.apiService.listCorporation()
        .subscribe(it => this.corporation.setValue(it[0]));
    }
    this.dialogRef.afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => this.storage.setItem(this.workshopKey, it));
  }

  get name() {
    return this.form.get('name');
  }

  get note() {
    return this.form.get('note');
  }

  get corporation() {
    return this.form.get('corporation');
  }

  private get preWorkshop(): Workshop {
    const {name, corporation} = this.storage.getItem(this.workshopKey) || Workshop.assign();
    return Workshop.assign({name, corporation});
  }

  static open(dialog: MatDialog, data: { workshop: Workshop }): MatDialogRef<WorkshopUpdateDialogComponent, Workshop> {
    return dialog.open(WorkshopUpdateDialogComponent, {panelClass: 'my-dialog', disableClose: true, data});
  }

  submit() {
    this.apiService.saveWorkshop(this.form.value)
      .subscribe(
        it => this.dialogRef.close(it),
        err => this.store.dispatch(new ShowError(err))
      );
  }
}


@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    WorkshopUpdateDialogComponent
  ],
  entryComponents: [
    WorkshopUpdateDialogComponent
  ],
  exports: [
    WorkshopUpdateDialogComponent
  ]
})
export class WorkshopUpdateDialogComponentModule {
}
