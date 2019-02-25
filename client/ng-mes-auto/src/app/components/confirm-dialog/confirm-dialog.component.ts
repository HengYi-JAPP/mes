import {ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material';

export class Options {
  textContent: 'Common.deleteConfirm';
  okText: 'Common.confirm';
  cancelText: 'Common.cancel';

  static assign(...sources: any[]): Options {
    const result = Object.assign(new Options(), ...sources);
    return result;
  }
}

@Component({
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfirmDialogComponent {
  constructor(@Inject(MAT_DIALOG_DATA) public data: any) {
  }

  static open(dialog: MatDialog, data = {
    textContent: 'Common.deleteConfirm',
    okText: 'Common.confirm',
    cancelText: 'Common.cancel'
  }): MatDialogRef<ConfirmDialogComponent, boolean> {
    return dialog.open(ConfirmDialogComponent, {data});
  }
}
