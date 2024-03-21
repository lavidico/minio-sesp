import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable, of, throwError} from 'rxjs';
import {Router} from '@angular/router';
import {catchError} from 'rxjs/operators';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private router: Router) {
  }


  /**
   * Método responsável pela verificação de um erro do tipo {@link HttpErrorResponse} com o código de erro
   * 401, 403 e 404. Caso o erro possua algum dos códigos mencionados o usuário será redirecionado para
   * a página designada para cada erro
   * @param err
   * @private
   */
  private handleAuthError(err: HttpErrorResponse): Observable<any> {
    if (err.status === 401 || err.status === 403) {
      this.router.navigate(['/erro/nao-autorizado']).then();
      return of(err.message);
    }
    if (err.status === 404) {
      // TODO: criar rota para Not Found
    }
    return throwError(err);
  }

  /**
   * Método responsável por interceptar todas as requisições realizadas pela aplicação
   *
   * @param request requisição realizada
   * @param next objeto responsável pela interceptação das requisições
   */
  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      // map((event: HttpEvent<any>) => {
      //   if (event instanceof HttpResponse) {
      //     console.log('event--->>>', event);
      //
      //   }
      //   return event;
      // }),
      catchError((error: HttpErrorResponse) => {
        return this.handleAuthError(error);
      }));
  }
}
