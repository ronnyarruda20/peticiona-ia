import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InteresseRequest, InteresseResponse } from './interesse';

@Injectable({ providedIn: 'root' })
export class InteresseService {
  private readonly http = inject(HttpClient);

  registrar(request: InteresseRequest): Observable<InteresseResponse> {
    return this.http.post<InteresseResponse>('/api/interesses', request);
  }
}
