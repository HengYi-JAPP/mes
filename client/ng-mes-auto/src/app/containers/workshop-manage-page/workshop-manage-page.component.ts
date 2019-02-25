import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';
import {MatDialog} from '@angular/material';
import {Store} from '@ngrx/store';
import {filter} from 'rxjs/operators';
import {WorkshopUpdateDialogComponent} from '../../components/workshop-update-dialog/workshop-update-dialog.component';
import {Workshop} from '../../models/workshop';
import {ApiService} from '../../services/api.service';
import {UtilService} from '../../services/util.service';
import {Delete, SaveSuccess} from '../../store/actions/workshop-manage-page';
import {workshopManagePageWorkshops} from '../../store/config';

@Component({
  templateUrl: './workshop-manage-page.component.html',
  styleUrls: ['./workshop-manage-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class WorkshopManagePageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-workshop-manage-page') b2 = true;
  readonly displayedColumns = ['corporation', 'name', 'btns'];
  readonly workshops$ = this.store.select(workshopManagePageWorkshops);

  constructor(private store: Store<any>,
              private dialog: MatDialog,
              private apiService: ApiService,
              private utilService: UtilService) {
  }

  create() {
    this.update(Workshop.assign());
  }

  update(workshop: Workshop) {
    WorkshopUpdateDialogComponent.open(this.dialog, {workshop})
      .afterClosed()
      .pipe(
        filter(it => !!it)
      )
      .subscribe(it => {
        this.store.dispatch(new SaveSuccess({workshop: it}));
        this.utilService.showSuccess();
      });
  }

  delete(workshop: Workshop) {
    this.utilService.showConfirm()
      .subscribe(() => this.store.dispatch(new Delete({id: workshop.id})));
  }

}
