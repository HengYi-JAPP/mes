import {ChangeDetectionStrategy, Component, HostBinding} from '@angular/core';

@Component({
  templateUrl: './product-plan-root-page.component.html',
  styleUrls: ['./product-plan-root-page.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ProductPlanRootPageComponent {
  @HostBinding('class.app-page') readonly b1 = true;
  @HostBinding('class.app-product-plan-root-page') readonly b2 = true;
}
