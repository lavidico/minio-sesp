import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BoletimOcorrencia} from '../../models';
import {Observable} from 'rxjs';
import {take} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {BoletimOcorrenciaSave} from '../../models/dtos';
import {AuthService} from '../autenticacao';

/**
 * Serviço responsável pelos endpoints referentes à {@link BoletimOcorrencia}
 */
@Injectable({
  providedIn: 'root'
})
export class BoletimOcorrenciaService {

  /**
   * URI do microsserviço de Gerenciamento de Ocorrência
   * @private
   */
  private readonly api = `${environment.api_gerenciamento_ocorrencia}/ocorrencias`;

  constructor(private http: HttpClient,
              private authService: AuthService) {
  }

  /**
   * Método responsável pela listagem de boletins de ocorrência no formato {@link BoletimOcorrencia}
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /ocorrencias
   *
   * @return {@code Observable<any>}
   */
  public listar(page: number): Observable<any> {
    return this.http.get<any>(`${this.api}?size=5&page=` + (page - 1), {headers: this.authService.getHeader()})
      .pipe(
        // tap(console.log),
        take(1)
      );
  }

  /**
   * Método responsável pela listagem de boletins de ocorrência no formato {@link BoletimOcorrencia}
   * <br>Roles: VT, AC, AD, CD
   * <br>Método: GET
   * <br>URI: /ocorrencias?contato-vitima={email}
   *
   * @return {@code Promise<BoletimOcorrencia[]>}
   */
  public async listarPorVitima(email: string, page: number): Promise<any> {
    const resultado = await this.http
      .get<any>(`${this.api}?email-vitima=${email}&size=5%page=${page-1}`, {headers: this.authService.getHeader()})
      .toPromise();
    return resultado;
  }

  /**
   * Método responsável pela alteração de boletim de ocorrência
   * <br>Roles: AC, AD
   * <br>Método: PUT
   * <br>URI: /ocorrencias/{id}
   *
   * @param boletimOcorrencia entidade do tipo {@link BoletimOcorrenciaSave} que será enviada para alteração.
   * @return {@code Observable<BoletimOcorrencia>}
   */
  alterar(boletimOcorrencia: BoletimOcorrenciaSave): Observable<BoletimOcorrencia> {
    return this.http.put<BoletimOcorrencia>(`${this.api}/${boletimOcorrencia.id}`, boletimOcorrencia, {headers: this.authService.getHeader()}).pipe(take(1));
  }

  /**
   * Método responsável pela busca por id de um boletim de ocorrência
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /ocorrencias/{id}
   *
   * @param id identificador único de boletim que será usado na consulta.
   * @return {@code Observable<BoletimOcorrencia>}
   */
  buscar(id: string): Observable<BoletimOcorrencia> {
    return this.http.get<BoletimOcorrencia>(`${this.api}/${id}`, {headers: this.authService.getHeader()}).pipe(take(1));
  }

  /**
   * Método responsável pelo cadastro de um novo boletim de ocorrência.
   * <br>Roles: AC, AD
   * <br>Método: POST
   * <br>URI: /ocorrencias
   *
   * @param boletimOcorrencia dados do boletim de ocorrência que será salvo no formato de {@link BoletimOcorrenciaSave}
   * @return {@code Observable<BoletimOcorrencia>}
   */
  criar(boletimOcorrencia: BoletimOcorrenciaSave): Observable<BoletimOcorrencia> {
    return this.http.post<BoletimOcorrencia>(`${this.api}`, boletimOcorrencia, {headers: this.authService.getHeader()})
      .pipe(
        // tap(console.log),
        take(1)
      );
  }

}
