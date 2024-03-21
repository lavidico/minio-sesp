import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {AuthService} from '../autenticacao';

@Injectable({
              providedIn: 'root'
            })
export class AuthGuard implements CanActivate {

  constructor(private router: Router,
              private autenticacaoService: AuthService) {
  }

  /**
   * Método responsável por verificar se o usuário que realizou a alteração de rota está autenticado
   * @param route
   * @param state
   */
  async canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Promise<boolean> {

    const isAutenticado = await this.autenticacaoService.isAutenticado();

    if (!isAutenticado) {
      await this.router.navigate(['/login']);
      return false;
    }
    return true;
  }

}
