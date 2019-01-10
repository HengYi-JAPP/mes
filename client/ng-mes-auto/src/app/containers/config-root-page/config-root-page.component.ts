import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';

@Component({
  templateUrl: './config-root-page.component.html',
  styleUrls: ['./config-root-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ConfigRootPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.app-config-root-page') readonly b2 = true;
}
