import {BrowserModule} from '@angular/platform-browser';
import {LOCALE_ID, NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BoletimOcorrenciaService, SuspeitoService, VitimaService} from './services/api-gerenciamento-ocorrencias';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {PessoaService} from './services/api-registro-geral';
import {RouterModule} from '@angular/router';
import {ModalModule} from 'ngx-bootstrap/modal';
import {registerLocaleData} from '@angular/common';
import ptBr from '@angular/common/locales/pt';
import {SharedModule} from './views/shared/shared.module';
import {AuthInterceptor} from './services/interceptors';

registerLocaleData(ptBr, 'pt-BR');

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
      BrowserModule,
      AppRoutingModule,
      HttpClientModule,
      RouterModule,
      ModalModule.forRoot(),
      SharedModule,
  ],
  providers: [
    PessoaService,
    BoletimOcorrenciaService,
    VitimaService,
    SuspeitoService,
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: LOCALE_ID, useValue: 'pt-BR'},
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
