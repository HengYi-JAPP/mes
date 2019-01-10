import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';

@Component({
  templateUrl: './admin-root-page.component.html',
  styleUrls: ['./admin-root-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminRootPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.app-admin-root-page') b2 = true;
}
