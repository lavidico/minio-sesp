import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../../services/autenticacao';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
/**
 *  Classe responsável por controlar o componente do menu
 */
export class NavbarComponent implements OnInit {

  nomeUsuario: string;
  permissao: string;

  constructor(private authService: AuthService) {
  }

  /**
   * Método executado na inicializçaão do Componente
   *
   * Busca o nome do usuário e sua permissão na classe de serviço de autenticação
   */
  ngOnInit(): void {
    this.nomeUsuario = this.authService.nomeUsuario;
    this.permissao = this.authService.permissaoUsuario;
  }

  /**
   * Método responsável por encessar a sessão do usuário quando ele clicar no botão Sair
   */
  logout(): void {
    this.authService.logout();
  }
}
