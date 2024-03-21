import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {AuthGuard} from './services';


const routes: Routes = [
  {
    path: 'boletim-ocorrencia',
    canActivate: [AuthGuard],
    loadChildren: () => import('./views/boletim-ocorrencia/boletim-ocorrencia.module').then(mod => mod.BoletimOcorrenciaModule)
  },
  {
    path: 'login',
    loadChildren: () => import('./views/login/login.module').then(mod => mod.LoginModule),
  },
  {
    path: 'erro',
    loadChildren: () => import('./views/erro/erro.module').then(mod => mod.ErroModule),
  },
  {
    path: '', pathMatch: 'full', redirectTo: 'login',
  },
  {
    path: '**', redirectTo: 'login',
  },
];

@NgModule({
            imports: [
              RouterModule.forRoot(routes),
            ],
            exports: [
              RouterModule
            ],

          })
export class AppRoutingModule {
}
