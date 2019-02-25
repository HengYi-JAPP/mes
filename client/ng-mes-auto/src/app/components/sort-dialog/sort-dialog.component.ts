import {ChangeDetectionStrategy, Component, HostBinding, Inject, NgModule} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';
import {BehaviorSubject} from 'rxjs';
import {isArray} from 'util';
import {downEle, upEle} from '../../services/util.service';
import {SharedModule} from '../../shared.module';

@Component({
  templateUrl: './sort-dialog.component.html',
  styleUrls: ['./sort-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SortDialogComponent {
  @HostBinding('class.app-dialog-comp') b1 = true;
  @HostBinding('class.app-sort-dialog.component') b2 = true;
  readonly datas$ = new BehaviorSubject([]);
  readonly disabled$ = new BehaviorSubject(true);
  readonly displayKeys: string[];

  constructor(private dialogRef: MatDialogRef<SortDialogComponent>,
              @Inject(MAT_DIALOG_DATA)  data: { datas: any[], displayKeys: string | string[] }) {
    const {datas, displayKeys} = data;
    this.datas$.next(datas || []);
    if (isArray(displayKeys)) {
      this.displayKeys = displayKeys as string[];
    } else {
      if (displayKeys) {
        this.displayKeys = [displayKeys as string];
      }
    }
  }

  static open<T>(dialog: MatDialog, data: { datas: T[], displayKeys: string | string[] }): MatDialogRef<SortDialogComponent, T[]> {
    return dialog.open(SortDialogComponent, {panelClass: 'my-dialog', data});
  }

  up(data: any) {
    const next = upEle(this.datas$.value, data);
    this.handleChange(next);
  }

  down(data: any) {
    const next = downEle(this.datas$.value, data);
    this.handleChange(next);
  }

  submit() {
    this.dialogRef.close(this.datas$.value);
  }

  private handleChange(next: any[]) {
    this.datas$.next(next);
    this.disabled$.next(false);
  }
}

@NgModule({
  imports: [
    SharedModule
  ],
  declarations: [
    SortDialogComponent
  ],
  entryComponents: [
    SortDialogComponent
  ],
  exports: [
    SortDialogComponent
  ]
})
export class SortDialogComponentModule {
}
