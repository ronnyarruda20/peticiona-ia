import {
  ApplicationConfig,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { routes } from './app.routes';
import { sessaoInterceptor } from './auth/sessao.interceptor';

// Sem isto, os pipes de data caem no inglês — e "Wednesday" numa tela de prazo
// processual brasileiro é erro de produto, não detalhe.
registerLocaleData(localePt, 'pt-BR');

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    // O interceptor manda para o login quando a sessão expira no meio do uso — a aba
    // do advogado fica aberta o dia inteiro.
    provideHttpClient(withFetch(), withInterceptors([sessaoInterceptor])),
    // withComponentInputBinding: o :id da rota chega como input() no componente,
    // sem ActivatedRoute espalhado pelos construtores.
    provideRouter(routes, withComponentInputBinding()),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
  ],
};
