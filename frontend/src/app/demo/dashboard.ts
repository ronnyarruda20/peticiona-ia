import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DemoService } from './demo.service';
import { AuthService } from '../auth/auth.service';
import { Dashboard as DashboardDados, LinhaIntimacao } from './demo';

/**
 * "Seu dia" — a primeira tela que o advogado abre.
 *
 * Dashboard estruturado: contadores, fila de intimações e prazos ordenados por
 * vencimento. Sem frase-resumo gerada por IA (corte #4, doc 04) — é charme, não função,
 * e um parágrafo de IA errado no topo do dia custa confiança.
 */
@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, DatePipe],
  templateUrl: './dashboard.html',
  styleUrl: './demo.css',
})
export class Dashboard {
  private readonly service = inject(DemoService);
  private readonly auth = inject(AuthService);

  /** Já resolvido pelo guard antes desta tela existir — aqui é só leitura. */
  readonly usuario = this.auth.usuario;

  readonly dados = signal<DashboardDados | null>(null);
  readonly erro = signal<string | null>(null);
  readonly reiniciando = signal(false);

  sair(evento: Event): void {
    evento.preventDefault();
    this.auth.sair();
  }

  constructor() {
    this.carregar();
  }

  carregar(): void {
    this.service.dashboard().subscribe({
      next: (d) => {
        this.dados.set(d);
        this.erro.set(null);
      },
      error: () => this.erro.set('Não consegui carregar o painel. O servidor está no ar?'),
    });
  }

  reiniciar(): void {
    this.reiniciando.set(true);
    this.service.reiniciar().subscribe({
      next: () => {
        this.reiniciando.set(false);
        this.carregar();
      },
      error: () => this.reiniciando.set(false),
    });
  }

  /** A cor da borda esquerda conta a história do item sem o usuário ler nada. */
  classe(i: LinhaIntimacao): string {
    if (i.temRascunho) return 'pronto';
    if (i.situacao === 'NAO_LIDA') return 'pendente';
    if (i.urgencia === 'ALTA' || i.precisaRevisao) return 'urgente';
    return '';
  }

  /** Dias corridos até o vencimento — o número que o advogado realmente olha. */
  diasRestantes(iso: string | null): number | null {
    if (!iso) return null;
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    const [a, m, d] = iso.split('-').map(Number);
    const venc = new Date(a, m - 1, d);
    return Math.round((venc.getTime() - hoje.getTime()) / 86_400_000);
  }
}
