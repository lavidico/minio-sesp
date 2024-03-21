import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Bairro, Cidade} from 'src/app/models';
import {environment} from 'src/environments/environment';
import {AuthService} from '../autenticacao';

@Injectable({
              providedIn: 'root'
            })
export class CidadeService {


  /**
   * URI do microsserviço de Registro Geral
   * @private
   */
  private readonly api = `${environment.api_registro_geral}/cidades`;

  constructor(private http: HttpClient, private authService: AuthService) {
  }


  /**
   * Método responsável pela busca das cidades {@link Cidade}
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /cidades
   *
   * @return {@code Promise<Cidade[]>}
   */
  public async listar(): Promise<Cidade[]> {
    const resultado = await this.http
                                .get<any>(`${this.api}`, {headers: this.authService.getHeader()})
                                .toPromise();
    return resultado.content;
  }

  /**
   * Método responsável pela listagem de bairros.
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /cidades/{idCidade}/bairros
   *
   * @param idCidade identificador da cidade, para busca por cidade
   * @return {@code Promise<Bairro[]>} Lista de bairros encontrados com base nos parâmetros informados
   */
  public async listarTodosBairros(idCidade: number): Promise<Bairro[]> {
    const result = await this.http
                             .get<any>(`${this.api}/${idCidade}/bairros?size=1000`, {headers: this.authService.getHeader()})
                             .toPromise();
    return result.content;
  }

  /**
   * Endpoint responsável pela busca de um bairro
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /cidades/{idCidade}/bairros/{idBairro}
   *
   * @param idCidade identificador da cidade, para busca por cidade
   * @param idBairro identificador único do bairro, utilizado para busca por identificador
   * @return {@code Promise<BairroDTO>} dados do bairro encontrado no formato {@link Bairro}
   */
  public async buscarBairro(idCidade: number, idBairro: number): Promise<Bairro> {
    return await this.http
                     .get<any>(`${this.api}/${idCidade}/bairros/${idBairro}`, {headers: this.authService.getHeader()})
                     .toPromise();
  }
}
