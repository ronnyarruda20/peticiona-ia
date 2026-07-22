import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Dashboard, DetalheIntimacao, RespostaProcessamento } from './demo';

@Injectable({ providedIn: 'root' })
export class DemoService {
  private readonly http = inject(HttpClient);

  /** Em produção o Spring Boot serve o front, então o caminho relativo resolve. */
  private readonly api = '/api/demo';

  dashboard(): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${this.api}/dashboard`);
  }

  intimacao(id: string): Observable<DetalheIntimacao> {
    return this.http.get<DetalheIntimacao>(`${this.api}/intimacoes/${id}`);
  }

  /**
   * Entrega a intimação ao fluxo de IA e volta na hora, com 202.
   *
   * <p>Ler e rascunhar acontecem lá, e levam minutos. Quem acompanha o resultado é o
   * polling em {@link intimacao} — esta chamada só enfileira.
   */
  processar(id: string): Observable<RespostaProcessamento> {
    return this.http.post<RespostaProcessamento>(`${this.api}/intimacoes/${id}/processar`, {});
  }

  /** Devolve o acervo ao estado inicial, para reapresentar do zero. */
  reiniciar(): Observable<unknown> {
    return this.http.post(`${this.api}/reiniciar`, {});
  }
}
