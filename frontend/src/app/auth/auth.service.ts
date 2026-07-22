import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

/** O advogado logado, como o backend o descreve em GET /api/me. */
export interface UsuarioLogado {
  email: string;
  nome: string | null;
  fotoUrl: string | null;
  oabNumero: string | null;
  oabUf: string | null;
  /** Quanto ainda dá para usar hoje — melhor avisar antes do que recusar depois. */
  execucoesRestantesHoje: number;
}

/**
 * Quem está logado.
 *
 * <p>A sessão vive num cookie que o navegador manda sozinho; não há token para guardar
 * em localStorage — e é por isso que não há token para alguém roubar de lá.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  /** `undefined` = ainda não perguntamos; `null` = perguntamos e não há sessão. */
  readonly usuario = signal<UsuarioLogado | null | undefined>(undefined);

  /**
   * Pergunta ao servidor quem somos.
   *
   * <p>401 aqui é resposta esperada, não falha: significa "ninguém logado". Por isso o
   * erro vira `null` em vez de estourar.
   */
  carregar(): Observable<UsuarioLogado | null> {
    return this.http.get<UsuarioLogado>('/api/me').pipe(
      catchError(() => of(null)),
      tap((u) => this.usuario.set(u)),
    );
  }

  /** OAuth2 é redirecionamento de navegador, não XHR — daí o `location.href`. */
  entrar(): void {
    window.location.href = '/oauth2/authorization/google';
  }

  sair(): void {
    this.http.post('/logout', {}).subscribe({
      next: () => this.aposSair(),
      error: () => this.aposSair(),
    });
  }

  salvarOab(numero: string, uf: string): Observable<unknown> {
    return this.http.post('/api/me/oab', { numero, uf }).pipe(
      tap(() =>
        this.usuario.update((u) => (u ? { ...u, oabNumero: numero, oabUf: uf } : u)),
      ),
    );
  }

  private aposSair(): void {
    this.usuario.set(null);
    // Recarrega em vez de navegar: descarta qualquer estado de tela do usuário anterior.
    window.location.href = '/login';
  }
}
