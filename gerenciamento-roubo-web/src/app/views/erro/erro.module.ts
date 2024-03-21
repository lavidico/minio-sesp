import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {ErroRoutingModule} from './erro-routing.module';
import {NaoAutorizadoComponent} from './nao-autorizado';
import {SharedModule} from '../shared/shared.module';


@NgModule({
            declarations: [NaoAutorizadoComponent],
            imports: [
              CommonModule,
              ErroRoutingModule,
              SharedModule,
            ]
          })
export class ErroModule {
}
