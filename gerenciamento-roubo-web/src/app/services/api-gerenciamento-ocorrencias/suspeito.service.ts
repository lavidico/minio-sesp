import {AuthService} from '../autenticacao';
import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {HttpClient} from '@angular/common/http';
import {SuspeitoSave} from '../../models/dtos';
import {Observable} from 'rxjs';
import {Suspeito} from '../../models';
import {take} from 'rxjs/operators';

/**
 * Serviço responsável pelos endpoints referentes à {@link Suspeito}
 */
@Injectable({
              providedIn: 'root',
            })
export class SuspeitoService {
  /**
   * URI do microsserviço de Gerenciamneto de Ocorrência relacionado ao {@link Suspeito}
   * @private
   */
  private readonly api = `${environment.api_gerenciamento_ocorrencia}/ocorrencias`;

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  /**
   * Método responsável pelo cadastro de um novo suspeito
   * <br>Roles: AC, AD
   * <br>Método: POST
   * <br>URI: /ocorrencias/suspeitos
   *
   * @param suspeito dados do suspeito que será salvo no formato {@link SuspeitoSave}
   * @param idOcorrencia identificador único do boletim de ocorrência relacionado ao suspeito
   * @return {@code Observable<Suspeito>}
   */
  criar(suspeito: SuspeitoSave, idOcorrencia: string): Observable<Suspeito> {
    return this.http
               .post<Suspeito>(`${this.api}/${idOcorrencia}/suspeitos`, suspeito, {
                 headers: this.authService.getHeader(),
               })
               .pipe(take(1));
  }

  /**
   * Método responsável pela listagem de suspeitos no formato {@link Suspeito} relacionados à um boletim de ocorrência
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /ocorrencia/{idOcorrencia}/suspeitos
   *
   * @param idOcorrencia identificador único do boletim de ocorrência que será utilizado na consulta
   * @return {@code Observable<Suspeito[]>}
   */
  listar(idOcorrencia: string): Observable<Suspeito[]> {
    return this.http
               .get<Suspeito[]>(`${this.api}/${idOcorrencia}/suspeitos`, {
                 headers: this.authService.getHeader(),
               })
               .pipe(take(1));
  }
}
