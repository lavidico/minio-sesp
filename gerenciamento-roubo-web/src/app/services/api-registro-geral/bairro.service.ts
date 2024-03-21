import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AuthService} from '../autenticacao';
import {Bairro} from '../../models';
import {environment} from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BairroService {

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  /**
   * URI do microsserviço de Registro Geral
   * @private
   */
  private readonly api = `${environment.api_registro_geral}/bairros`;

  /**
   * Endpoint responsável pela busca de um bairro
   * <br>Roles: TI, VT, AC, AD, RH, PL
   * <br>Método: GET
   * <br>URI: /bairros/{idBairro}
   *
   * @param idBairro identificador único do bairro, utilizado para busca por identificador
   * @return {@code Promise<Bairro>} dados do bairro encontrado no formato {@link Bairro}
   */
  public async buscar(idBairro: number): Promise<Bairro> {
    return await this.http
                     .get<any>(`${this.api}/${idBairro}`, {headers: this.authService.getHeader()})
                     .toPromise();
  }
}
