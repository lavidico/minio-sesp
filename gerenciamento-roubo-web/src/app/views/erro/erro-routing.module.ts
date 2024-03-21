import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {NaoAutorizadoComponent} from './nao-autorizado/nao-autorizado.component';

const routes: Routes = [
  {path: 'nao-autorizado', component: NaoAutorizadoComponent}
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ErroRoutingModule { }
