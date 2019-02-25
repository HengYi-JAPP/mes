import {NgModule, Optional, SkipSelf} from '@angular/core';
import {EffectsModule} from '@ngrx/effects';
import {StoreRouterConnectingModule} from '@ngrx/router-store';
import {StoreModule} from '@ngrx/store';
import {StoreDevtoolsModule} from '@ngrx/store-devtools';
import {environment, httpInterceptorProviders, metaReducers} from '../environments/environment';
import {AppNavbarComponent} from './components/app-navbar/app-navbar.component';
import {ConfirmDialogComponent} from './components/confirm-dialog/confirm-dialog.component';
import {ShellPageComponent} from './containers/shell-page/shell-page.component';
import {CoreRoutingModule} from './core-routing.module';
import {SharedModule} from './shared.module';
import {reducers} from './store/core';
import {CoreEffects} from './store/effects/core.effects';

@NgModule({
  declarations: [
    ShellPageComponent,
    AppNavbarComponent,
    ConfirmDialogComponent
  ],
  entryComponents: [
    ConfirmDialogComponent
  ],
  imports: [
    StoreModule.forRoot(reducers, {metaReducers}),
    EffectsModule.forRoot([CoreEffects]),
    StoreRouterConnectingModule.forRoot({stateKey: 'router'}),
    !environment.production ? StoreDevtoolsModule.instrument() : [],
    SharedModule,
    CoreRoutingModule
  ],
  providers: [
    httpInterceptorProviders
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}
