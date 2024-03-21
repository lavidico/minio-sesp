import { Component, OnInit } from '@angular/core';
import {AuthService} from '../../../services/autenticacao';
import {Router} from '@angular/router';

@Component({
  selector: 'app-nao-autorizado',
  templateUrl: './nao-autorizado.component.html',
  styleUrls: ['./nao-autorizado.component.css']
})
/**
 * Classe responsável por controlar o componente de Não Autorizado
 */
export class NaoAutorizadoComponent implements OnInit {

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
  }

  /**
   * Método responsável por redirecionar o usuário para a página de login
   * e encerrar sua sessão.
   */
  async voltarParaHome(): Promise<void> {
    this.authService.logout();
    await this.router.navigate(['/login']);
  }
}
