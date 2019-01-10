import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';

@Component({
  templateUrl: './report-root-page.component.html',
  styleUrls: ['./report-root-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ReportRootPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.app-report-root-page') readonly b2 = true;
}
