import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {LoginRoutingModule} from './login-routing.module';
import {LoginComponent} from './login';
import {ReactiveFormsModule} from '@angular/forms';
import {AuthService} from '../../services/autenticacao';


@NgModule({
  declarations: [
    LoginComponent
  ],
  imports: [
    CommonModule,
    LoginRoutingModule,
    ReactiveFormsModule
  ],
  providers: [
    AuthService,
  ]
})
export class LoginModule {
}
