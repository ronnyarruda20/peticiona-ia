import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CalculoRequest, ResultadoPrazo } from './prazo';

@Injectable({ providedIn: 'root' })
export class PrazoService {
  private readonly http = inject(HttpClient);

  /** Em produção o Spring Boot serve o front, então o caminho relativo resolve. */
  private readonly api = '/api/prazos';

  calcular(req: CalculoRequest): Observable<ResultadoPrazo> {
    return this.http.post<ResultadoPrazo>(`${this.api}/calcular`, req);
  }
}
