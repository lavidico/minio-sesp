import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {BoletimOcorrenciaRoutingModule} from './boletim-ocorrencia-routing.module';
import {ListarBoletimComponent} from './listar-boletim';
import {CadastrarBoletimComponent} from './cadastrar-boletim';
import {ReactiveFormsModule, FormsModule} from '@angular/forms';
import {SharedModule} from '../shared/shared.module';
import {NgxPaginationModule} from 'ngx-pagination';


@NgModule({
            declarations: [
              ListarBoletimComponent,
              CadastrarBoletimComponent,
            ],
    imports: [
        CommonModule,
        BoletimOcorrenciaRoutingModule,
        ReactiveFormsModule,
        SharedModule,
        NgxPaginationModule,
        FormsModule,
    ],
            providers: []
          })
export class BoletimOcorrenciaModule {
}
