import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {take} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import jwt_decode from 'jwt-decode';
import {Observable} from 'rxjs';

export const JWT_DATA: string = 'jwt_data';
export const DATA_EXP: string = 'data_exp';

@Injectable({providedIn: 'root'})
export class AuthService {

  private readonly secondsInTimestampToExpire = 300;

  constructor(private http: HttpClient) {
  }

  /**
   * Método responsável por autenticar um usuário baseado no login e senha.
   * <br>URI: /autenticacao/keycloak/login
   *
   * @param login login do usuário que será autenticado
   * @param senha senha do usuário que será autenticada
   */
  public autenticar(login: string, senha: string): Observable<AuthResponse> {

    const requisicao = {
      usuario: login,
      senha: senha,
    };

    this.setDataExpToken();
    return this.http
               .post<AuthResponse>(`${environment.api_autenticacao}/keycloak/login`, requisicao)
               .pipe(take(1));

  }

  /**
   * Método responsável por realizar login social através do token criado pela API da google
   * <br>URI: autenticacao/google/login
   *
   * @param token token gerado pelo google
   * @return
   */
  public autenticarGoogle(token: string): Observable<any> {
    this.setDataExpToken();

    const headers = {
      'Authorization': `Bearer ${token}`,
    };
    return this.http.post<any>(`${environment.api_autenticacao}/google/login`, null,
                               {headers})
               .pipe(take(1));
  }

  /**
   * Método responsável por atualizar o token utilizado para autenticar o usuário
   * <br>URI: /autenticacao/keycloak/refresh
   *
   * @param refresh string de refresh extraída do token jwt
   */
  public async refreshToken(refresh: string): Promise<boolean> {
    const refreshTK = {
      refreshToken: refresh
    };

    try {
      const response = await this.http
                                 .post<AuthResponse>(`${environment.api_autenticacao}/keycloak/refresh`, refreshTK)
                                 .pipe(take(1))
                                 .toPromise();
      this.setToken(JSON.stringify(response));
      this.setDataExpToken();

      return true;
    } catch (e) {
    }
    return false;
  }

  /**
   * Método responsável por armazenar o token de autenticação no localstorage do browser
   * @param data string que será armazenada no localstorage
   */
  public setToken(data: string): void {
    localStorage.setItem(JWT_DATA, data);
  }

  /**
   * Método responsável por decodificar o token de acesso aplicando a função jwt_decode<>()
   * na string armazenada no localstorage
   */
  public decodeToken(): any {
    const auth = this.authenticationData;
    const decoded = jwt_decode<any>(auth.access_token);
    return decoded;
  }

  /**
   * Método responsável por verificar se existe um usuário logado
   */
  public isLoggedIn(): boolean {
    return this.authenticationData !== null;
  }

  /**
   * Realiza o logout do usuário removendo o token jwt do localstorage
   */
  public logout(): void {
    return localStorage.removeItem(JWT_DATA);
  }

  /**
   * Método responsável por gerar o header de autenticação para as requisições realizadas pela aplicação
   */
  public getHeader(): any {
    const token = this.authenticationData;
    const headers = {Authorization: `Bearer ${token.access_token}`};
    return headers;
  }

  /**
   * Método responsável por extrair o token jwt do localstorage no formato {@link any}
   */
  public get authenticationData(): any {
    return JSON.parse(localStorage.getItem(JWT_DATA));
  }

  /**
   * Método responsável por extrair o nome do usuário logado do token decodificado
   */
  public get nomeUsuario(): string {
    const name = this.decodeToken().name;
    return name;
  }

  public get emailUsuario(): string {
    const email = this.decodeToken().email;
    return email;
  }

  /**
   * Método responsável por extrair o token de acesso do token jwt
   */
  public get accessToken(): string {
    return this.authenticationData.access_token;
  }

  /**
   * Método responsável por extrair as permissões que o usuário possui
   */
  public get permissaoUsuario(): string {
    const decodedToken = this.decodeToken();
    const role = decodedToken?.resource_access['gerenciamento-roubo']?.roles[0];

    return permissoes.get(role);
  }

  /**
   * Método responsável por extrair a data de validade do token jwt no formato timestamp
   */
  public get dataValidadeToken(): Date {
    const dataExpStr = +localStorage.getItem(DATA_EXP);

    const dataExp = new Date(dataExpStr);
    return dataExp;
  }

  /**
   * Método responsável por checar se o token jwt ainda é válido
   */
  public checaTokenValido(): boolean {
    const dataAtual = new Date();
    return this.dataValidadeToken.valueOf() > dataAtual.valueOf();
  }

  /**
   * Método responsável por checar se o usuário foi autenticado
   */
  public async isAutenticado(): Promise<boolean> {
    const accessToken = this.accessToken;

    if (accessToken) {
      const isTokenValido = this.checaTokenValido();
      if (isTokenValido) {
        return true;
      }

      const refreshToken = this.authenticationData.refresh_token;

      if (await this.refreshToken(refreshToken)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Método responsável por salvar na localstorage do browser a data de vencimento do token no formato de timestamp
   * @private
   */
  private setDataExpToken(): void {
    const data = new Date();
    data.setSeconds(data.getSeconds() + this.secondsInTimestampToExpire);
    localStorage.setItem(DATA_EXP, data.getTime().toString());
  }

}

export const permissoes: Map<string, string> = new Map<string, string>();
permissoes.set('TI', 'Técnico em Informática');
permissoes.set('AC', 'Atendimento Ciosp');
permissoes.set('AD', 'Atendimento Delegacia');
permissoes.set('PL', 'Politec');
permissoes.set('RH', 'Recursos Humanos');
permissoes.set('VT', 'Viatura');
permissoes.set('CD', 'Cidadão');

interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  refreshExpires_in: number;
  refreshToken: string;
  tokenType: string;
  notBeforePolicy: number;
  sessionState: string;
  scope: string;
}
