import { Routes } from '@angular/router';

/**
 * Duas superfícies distintas:
 *  - `/calculadora` — pública, sem cadastro, ímã de leads (doc 07).
 *  - `/` e `/intimacoes/:id` — o produto: o fluxo do aha moment.
 *
 * Lazy loading em tudo: quem entra pela calculadora não baixa o painel.
 */
export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./demo/dashboard').then((m) => m.Dashboard),
    title: 'Seu dia · Peticiona',
  },
  {
    path: 'intimacoes/:id',
    loadComponent: () => import('./demo/intimacao').then((m) => m.Intimacao),
    title: 'Intimação · Peticiona',
  },
  {
    path: 'calculadora',
    loadComponent: () => import('./calculadora/calculadora').then((m) => m.Calculadora),
    title: 'Calculadora de prazos',
  },
  { path: '**', redirectTo: '' },
];
