import {Component, HostBinding} from '@angular/core';

@Component({
  templateUrl: './shell-page.component.html',
  styleUrls: ['./shell-page.component.less']
})
export class ShellPageComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-shell-page') b2 = true;
}
