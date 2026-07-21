import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Dashboard,
  DetalheIntimacao,
  RespostaClassificacao,
  RespostaRascunho,
} from './demo';

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

  /** A IA lê a publicação; o motor determinístico devolve a data. Pode levar alguns segundos. */
  classificar(id: string): Observable<RespostaClassificacao> {
    return this.http.post<RespostaClassificacao>(`${this.api}/intimacoes/${id}/classificar`, {});
  }

  /** A IA rascunha a peça. Mais lento que a classificação — a tela precisa mostrar isso. */
  rascunhar(id: string): Observable<RespostaRascunho> {
    return this.http.post<RespostaRascunho>(`${this.api}/intimacoes/${id}/rascunhar`, {});
  }

  /** Devolve o acervo ao estado inicial, para reapresentar do zero. */
  reiniciar(): Observable<unknown> {
    return this.http.post(`${this.api}/reiniciar`, {});
  }
}
