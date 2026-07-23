import { Component, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DemoService } from './demo.service';
import { AuthService } from '../auth/auth.service';
import { Dashboard as DashboardDados, LinhaIntimacao, ProcessoPendente } from './demo';

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

  /** Processos do DJEN esperando o advogado dizer de que lado está. */
  readonly pendentes = signal<ProcessoPendente[]>([]);
  /** O id do processo em confirmação, para travar só o cartão clicado. */
  readonly confirmando = signal<string | null>(null);

  sair(evento: Event): void {
    evento.preventDefault();
    this.auth.sair();
  }

  /**
   * O advogado escolheu de que lado está. Só depois disto a IA pode rodar no processo.
   *
   * <p>Recarregar o painel inteiro é de propósito: confirmar o cliente muda o cartão de
   * confirmação, as etiquetas das intimações daquele processo e a disponibilidade do botão
   * de IA — mais simples e menos sujeito a erro do que remendar cada um na mão.
   */
  confirmar(processoId: string, polo: 'A' | 'P'): void {
    this.confirmando.set(processoId);
    this.service.confirmarCliente(processoId, polo).subscribe({
      next: () => {
        this.confirmando.set(null);
        this.carregar();
        this.carregarPendentes();
      },
      error: (e) => {
        this.confirmando.set(null);
        this.erro.set(e?.error?.erro ?? 'Não consegui confirmar o cliente. Tente de novo.');
      },
    });
  }

  private carregarPendentes(): void {
    this.service.processosPendentes().subscribe({
      next: (p) => this.pendentes.set(p),
      error: () => this.pendentes.set([]),
    });
  }

  constructor() {
    this.carregar();
    this.carregarPendentes();
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

  /** Carrega ou remove os exemplos, conforme já estejam presentes. Nunca toca no DJEN real. */
  alternarExemplos(): void {
    this.reiniciando.set(true);
    const tem = this.dados()?.temExemplos ?? false;
    const acao = tem ? this.service.removerExemplos() : this.service.carregarExemplos();
    acao.subscribe({
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
