import {AuthService} from '../autenticacao';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {take} from 'rxjs/operators';
import {Pessoa} from 'src/app/models';
import {PessoaSave} from 'src/app/models/dtos';
import {environment} from 'src/environments/environment';

/**
 * Serviço responsável pelos endpoints referentes à {@link Pessoa}
 */
@Injectable({
  providedIn: 'root',
})
export class PessoaService {

  /**
   * URI do microsserviço de Registro Geral
   * @private
   */
  private readonly api = `${environment.api_registro_geral}/pessoas`;

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  /**
   * Método responsável pela busca por id de uma pessoa.
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /pessoas/{id}
   *
   * @param id identificador único da pessoa que será utilizado na consulta
   * @return {@code Promise<Pessoa>}
   */
  public buscar(id: number): Promise<Pessoa> {
    return this.http
      .get<Pessoa>(`${this.api}/${id}`, {
        headers: this.authService.getHeader(),
      }).toPromise();
  }

  /**
   * Método responsável pelo cadastro de uma nova pessoa.
   * <br>Roles: TI, PL
   * <br>Método: POST
   * <br>URI: /pessoas
   *
   * @param pessoa dados da pessoa que será salvo no formato de {@link PessoaSave}
   * @return {@code Observable<Pessoa>}
   */
  criar(pessoa: PessoaSave): Observable<Pessoa> {
    return this.http
      .post<Pessoa>(`${this.api}`, pessoa, {
        headers: this.authService.getHeader(),
      })
      .pipe(take(1));
  }

  /**
   * Método responsável por realizar uma busca de pessoa de acordo com o parâmetro passado.
   * Os parâmetros possíveis são:<br>
   *   * Nome<br>
   *   * Nome da mãe<br>
   *   * CPF<br>
   *   * Data de nascimento
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /pessoas?{parametro}={valor}
   *
   * @param parametro parâmetro que será utilizado na consulta
   * @param valor valor do paramâmetro que será utilizado na consulta
   * @return {@code Promise<Pessoa[]>}
   */
  async buscarPorParametro(parametro: string, valor: string): Promise<Pessoa[]> {
    const result = await this.http
      .get<any>(`${this.api}?${parametro}=${valor}`, {
        headers: this.authService.getHeader(),
      }).toPromise();
    return result.content;
  }

  /**
   * Método responsável por realizar uma busca de pessoa de acordo com o parâmetro passado.
   * Os parâmetros possíveis são:<br>
   *   * Nome<br>
   *   * Nome da mãe<br>
   *   * CPF<br>
   *   * Data de nascimento
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /pessoas?{parametro}={valor}&size=5&page=${page-1}
   *
   * @return {@code Promise<any>}
   * @param param parâmetro que será utilizado na consulta
   * @param value valor do paramâmetro que será utilizado na consulta
   * @param page número da página que irá ser consultada
   */
  async buscaPaginada(param: string, value: string, page: number): Promise<any> {
    const query = await this.http
      .get<any>(`${this.api}?${param}=${value}&size=5&page=${page-1}`, {
        headers: this.authService.getHeader(),
      }).toPromise();
    return query;
  }

  /**
   * Método responsável pela a alteração dos dados de uma pessoa
   * <br>Roles: TI, PL
   * <br>Método: PUT
   * <br>URI: /pessoas/{id}
   *
   * @param pessoa entidade do tipo {@link PessoaSave} que será enviada para alteração.
   * @return {@code Observable<Pessoa>}
   */
  alterar(pessoa: PessoaSave): Observable<Pessoa> {
    return this.http
      .put<Pessoa>(`${this.api}/${pessoa.id}`, pessoa, {
        headers: this.authService.getHeader(),
      })
      .pipe(take(1));
  }
}
