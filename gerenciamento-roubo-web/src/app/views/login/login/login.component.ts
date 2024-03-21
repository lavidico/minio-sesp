import {Component, NgZone, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {AuthService} from '../../../services/autenticacao';
import {Router} from '@angular/router';
import {environment} from '../../../../environments/environment';

declare const gapi: any;

@Component({
             selector: 'app-login',
             templateUrl: './login.component.html',
             styleUrls: ['./login.component.css']
           })
/**
 * Class responsável por controlar o componente de Login
 */
export class LoginComponent implements OnInit {


  readonly _versao = environment.version;

  loginForm: FormGroup;
  _enviado: boolean;
  erroLogin: string = undefined;
  public auth2: any;

  constructor(private formBuilder: FormBuilder,
              private zone: NgZone,
              private router: Router,
              private authService: AuthService) {
  }

  /**
   * Método responsável por trazer os campos do formulário de login
   *
   */
  get form() {
    return this.loginForm.controls;
  }

  get enviado(): boolean {
    return this._enviado;
  }

  get versao(): string {
    return this._versao;
  }

  /**
   * Método executado na inicialização do componente.
   * Monta o formulário de login
   */
  ngOnInit(): void {
    this._enviado = false;
    this.loginForm = this.formBuilder.group({
                                              login: this.formBuilder.control(
                                                '',
                                                [
                                                  Validators.required,
                                                  // Validators.email,
                                                ]
                                              ),
                                              senha: this.formBuilder.control(
                                                '',
                                                [
                                                  Validators.required
                                                ]
                                              )
                                            });
  }

  /**
   * Método responsável por realizar o login com os dados inseridos nos campos.
   *
   * @return vazio
   */
  realizarLogin(): void {
    const login: string = this.loginForm.value.login;
    const senha: string = this.loginForm.value.senha;

    this._enviado = true;

    this.authService
        .autenticar(login, senha)
        .subscribe(async (response) => {
          this.authService.setToken(JSON.stringify(response));
          await this.router.navigate(['boletim-ocorrencia']);
        }, error => {
          this.adicionaMensagemErro();
        });
  }


  public googleInit() {
    gapi.load('auth2', () => {
      this.auth2 = gapi.auth2.init({
                                     client_id: '349521170162-fqtffu7ltr4pkb8io1jema3n8ocaku1r.apps.googleusercontent.com',
                                     cookiepolicy: 'single_host_origin',
                                     scope: 'profile email'
                                   });
      this.criaBotaoLoginGoogle(document.getElementById('botaoGoogle'));
    });
  }

  public criaBotaoLoginGoogle(element) {
    this.auth2.attachClickHandler(element, {},
                                   (googleUser) => {
                                    this.authService.autenticarGoogle(googleUser.getAuthResponse().id_token)
                                        .subscribe( async (response) => {

                                          this.authService.setToken(JSON.stringify(response));

                                          await this.zone.run(  () => {
                                             this.router.navigate(['boletim-ocorrencia']);
                                          });
                                        }, error => {
                                          this.adicionaMensagemErro();
                                        });
                                  },
                                  (error) => alert(JSON.stringify(error, undefined, 2))
    );
  }

  adicionaMensagemErro(): void {
    this.erroLogin = 'Não foi possível realizar o Login. Tente novamente';
  }

  ngAfterViewInit() {
    this.googleInit();
  }
}
