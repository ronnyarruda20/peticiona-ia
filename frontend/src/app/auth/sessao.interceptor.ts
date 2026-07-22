import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Sessão expirada no meio do uso vira ida ao login, não um erro sem sentido na tela.
 *
 * <p>Sessões duram horas e o advogado deixa a aba aberta o dia todo. Sem isto, a primeira
 * ação depois da expiração falharia com uma mensagem genérica e ele não saberia que basta
 * entrar de novo.
 *
 * <p>Chamadas a `/api/me` são exceção: 401 ali é a resposta normal para "ninguém logado",
 * e redirecionar por causa dela criaria um laço com o próprio guard.
 */
export const sessaoInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const auth = inject(AuthService);

  return next(req).pipe(
    catchError((erro: HttpErrorResponse) => {
      if (erro.status === 401 && !req.url.startsWith('/api/me')) {
        auth.usuario.set(null);
        router.navigate(['/login']);
      }
      return throwError(() => erro);
    }),
  );
};
