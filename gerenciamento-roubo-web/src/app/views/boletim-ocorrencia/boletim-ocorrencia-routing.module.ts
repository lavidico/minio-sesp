import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ListarBoletimComponent} from './listar-boletim';
import {CadastrarBoletimComponent} from './cadastrar-boletim';
import {AuthGuard} from '../../services';

const routes: Routes = [
  {path: '', redirectTo: 'listar', canActivate: [AuthGuard]},
  {path: 'listar', component: ListarBoletimComponent, canActivate: [AuthGuard]},
  {path: 'cadastrar', component: CadastrarBoletimComponent, canActivate: [AuthGuard]},
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class BoletimOcorrenciaRoutingModule { }
