import {Component, HostBinding} from '@angular/core';
import {Title} from '@angular/platform-browser';
import {Store} from '@ngrx/store';
import {TranslateService} from '@ngx-translate/core';
import {FetchAuthInfo} from '../../store/actions/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.less']
})
export class AppComponent {
  @HostBinding('class.app-page') b1 = true;
  @HostBinding('class.app-root') b2 = true;

  constructor(private store: Store<any>,
              private title: Title,
              private translate: TranslateService) {
    this.store.dispatch(new FetchAuthInfo());

    this.translate.setDefaultLang('zh_CN');
    this.translate.use('zh_CN');
    this.translate.get('title').subscribe(it => title.setTitle(it));
  }

}
