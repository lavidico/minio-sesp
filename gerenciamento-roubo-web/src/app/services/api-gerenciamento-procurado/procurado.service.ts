import {AuthService} from '../autenticacao';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {take} from 'rxjs/operators';
import {Procurado} from 'src/app/models';
import {environment} from 'src/environments/environment';
import {ProcuradoSave} from '../../models/dtos';

/**
 * Serviço responsável pelos endpoints referentes à {@link Procurado}
 */
@Injectable({
  providedIn: 'root'
})
export class ProcuradoService {

  /**
   * URI do microsserviço de Gerenciamento de Procurados
   * @private
   */
  private readonly path = `${environment.api_gerenciamento_procurado}/procurados`;

  constructor(private http: HttpClient, private authService: AuthService) {
  }

  /**
   * Método responsável pela listagem de procurados no formato {@link Procurado}
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /procurados
   *
   * @return {@code Observable<Procurado[]>}
   */
  public listar(): Observable<Procurado[]> {
    return this.http.get<Procurado[]>(this.path, {headers: this.authService.getHeader()}).pipe(take(1));
  }

  /**
   * Método responsável pela busca por id de um procurado
   * <br>Roles: VT, AC, AD
   * <br>Método: GET
   * <br>URI: /procurados/{id}
   *
   * @param id identificador único de procurado que será usado na consulta.
   * @return {@code Observable<Procurado>}
   */
  public buscar(id: string): Observable<Procurado> {
    return this.http.get<Procurado>(`${this.path}/${id}`, {headers: this.authService.getHeader()}).pipe(take(1));
  }

  /**
   * Método responsável pelo cadastro de um novo procurado.
   * <br>Roles: AC, AD
   * <br>Método: POST
   * <br>URI: /procurados
   *
   * @param procurado dados do procurado que será salvo no formato {@link Procurado}
   * @return {@code Promise<Procurado>}
   */
  async criar(procurado: ProcuradoSave): Promise<Procurado> {
    return await this.http.post<Procurado>(`${this.path}`, procurado, {headers: this.authService.getHeader()}).toPromise();
  }

  /**
   * Método responsável pela alteração de procurado.
   * <br>Roles: AC, AD
   * <br>Método: PUT
   * <br>URI: /procurados/{id}
   *
   * @param procurado entidade do tipo {@link Procurado} que será enviado para alteração
   * @return {@code Observable<Procurado>}
   */
  public alterar(procurado: Procurado): Observable<Procurado> {
    return this.http.put<Procurado>(`${this.path}/${procurado.id}`, procurado, {headers: this.authService.getHeader()}).pipe(take(1));
  }

}
