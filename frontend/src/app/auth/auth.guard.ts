import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs/operators';
import { AuthService } from './auth.service';

/**
 * Barra as telas do acervo para quem não entrou.
 *
 * <p>Na primeira navegação ainda não sabemos quem é o usuário, então o guard pergunta ao
 * servidor e espera a resposta. Nas seguintes, o valor já está no signal e a decisão é
 * imediata — sem uma chamada de rede por clique.
 */
export const autenticado: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const conhecido = auth.usuario();
  if (conhecido !== undefined) {
    return conhecido ? true : router.createUrlTree(['/login']);
  }

  return auth.carregar().pipe(map((u) => (u ? true : router.createUrlTree(['/login']))));
};

/**
 * O contrário: quem já entrou não deve ver a tela de login.
 *
 * <p>Sem isto, voltar para `/login` depois de logado mostra um botão "Entrar" para quem
 * já está dentro — e clicar nele refaz todo o ciclo do OAuth sem necessidade.
 */
export const visitante: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const conhecido = auth.usuario();
  if (conhecido !== undefined) {
    return conhecido ? router.createUrlTree(['/']) : true;
  }

  return auth.carregar().pipe(map((u) => (u ? router.createUrlTree(['/']) : true)));
};
