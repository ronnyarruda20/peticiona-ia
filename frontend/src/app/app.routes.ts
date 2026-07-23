import { Routes } from '@angular/router';
import { autenticado, semOab, visitante } from './auth/auth.guard';

/**
 * Duas superfícies distintas, e elas têm regras de acesso diferentes:
 *  - `/calculadora` — pública, sem cadastro, ímã de leads (doc 07).
 *  - `/` e `/intimacoes/:id` — o acervo: processos de clientes reais, só com login.
 *
 * Lazy loading em tudo: quem entra pela calculadora não baixa o painel.
 */
export const routes: Routes = [
  {
    path: '',
    canActivate: [autenticado],
    loadComponent: () => import('./demo/dashboard').then((m) => m.Dashboard),
    title: 'Seu dia · Peticiona',
  },
  {
    path: 'intimacoes/:id',
    canActivate: [autenticado],
    loadComponent: () => import('./demo/intimacao').then((m) => m.Intimacao),
    title: 'Intimação · Peticiona',
  },
  {
    path: 'login',
    canActivate: [visitante],
    loadComponent: () => import('./auth/login').then((m) => m.Login),
    title: 'Entrar · Peticiona',
  },
  {
    path: 'comecar',
    canActivate: [semOab],
    loadComponent: () => import('./auth/onboarding-oab').then((m) => m.OnboardingOab),
    title: 'Sua OAB · Peticiona',
  },
  {
    path: 'calculadora',
    loadComponent: () => import('./calculadora/calculadora').then((m) => m.Calculadora),
    title: 'Calculadora de prazos',
  },
  { path: '**', redirectTo: '' },
];
