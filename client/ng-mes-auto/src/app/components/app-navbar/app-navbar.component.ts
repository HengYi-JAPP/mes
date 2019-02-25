import {ChangeDetectionStrategy, Component} from '@angular/core';
import {Store} from '@ngrx/store';
import {coreAuthAdmin, coreLoading} from '../../store/core';

@Component({
  selector: 'app-navbar',
  templateUrl: './app-navbar.component.html',
  styleUrls: ['./app-navbar.component.less'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppNavbarComponent {
  readonly coreLoading$ = this.store.select(coreLoading);
  readonly admin$ = this.store.select(coreAuthAdmin);

  constructor(private store: Store<any>) {
  }
}
