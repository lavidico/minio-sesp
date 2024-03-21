import {AuthService} from '../autenticacao';
import {Injectable} from '@angular/core';
import {Vitima} from '../../models';
import {VitimaSave} from '../../models/dtos';
import {Observable} from 'rxjs';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {take} from 'rxjs/operators';

/**
 * Serviço responsável pelos endpoints referentes à {@link Vitima}
 */
@Injectable({
              providedIn: 'root'
            })
export class VitimaService {

  /**
   * URI do microsserviço de Gerenciamento de Ocorrência relacionado ao {@link Vitima}
   * @private
   */
  private readonly api = `${environment.api_gerenciamento_ocorrencia}/ocorrencias`;

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  /**
   * Método responsável pelo cadastro de uma nova vitima
   * <br>Roles: AC, AD
   * <br>Método: POST
   * <br>URI: /ocorrencias/vitimas
   *
   * @param vitima dados do suspeito que será salvo no formato {@link VitimaSave}
   * @param idOcorrencia identificador único do boletim de ocorrência relacionado à vitima
   * @return {@code Observable<Vitima>}
   */
  criar(vitima: VitimaSave, idOcorrencia: string): Observable<Vitima> {
    return this.http.post<Vitima>(`${this.api}/${idOcorrencia}/vitimas`, vitima, {headers: this.authService.getHeader()}).pipe(take(1));
  }

  /**
   * Método responsável pela listagem de vitimas no formato {@link Vitima} relacionados à um boletim de ocorrência
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /ocorrencia/{idOcorrencia}/vitimas
   *
   * @param idOcorrencia identificador único do boletim de ocorrência que será utilizado na consulta
   * @return {@code Observable<Vitima[]>}
   */
  listar(idOcorrencia: string): Observable<Vitima[]> {
    return this.http.get<Vitima[]>(`${this.api}/${idOcorrencia}/vitimas`, {headers: this.authService.getHeader()}).pipe(take(1));
  }
}
