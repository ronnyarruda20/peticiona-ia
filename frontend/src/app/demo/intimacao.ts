import { Component, computed, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ResultadoPrazo } from '../prazo';
import { DemoService } from './demo.service';
import { Classificacao, DetalheIntimacao } from './demo';

/**
 * O aha moment inteiro numa tela só.
 *
 *   publicação bruta → [Ler com a IA] → leitura + prazo com memória de cálculo
 *                    → [Rascunhar a peça] → editor
 *
 * Três decisões de produto visíveis aqui:
 *  - A publicação bruta fica na tela. É o "antes" que dá sentido ao "depois".
 *  - A confiança da IA é mostrada como barra, não escondida. Incerteza é informação.
 *  - O prazo nunca aparece sem a memória de cálculo ao lado (doc 10).
 */
@Component({
  selector: 'app-intimacao',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './intimacao.html',
  styleUrl: './demo.css',
})
export class Intimacao {
  /** Vem da rota /intimacoes/:id (withComponentInputBinding). */
  readonly id = input.required<string>();

  private readonly service = inject(DemoService);

  readonly dados = signal<DetalheIntimacao | null>(null);
  readonly prazo = signal<ResultadoPrazo | null>(null);
  readonly rascunho = signal<string>('');

  readonly lendo = signal(false);
  readonly erro = signal<string | null>(null);

  private polling: ReturnType<typeof setInterval> | null = null;

  readonly classificacao = computed<Classificacao | null>(() => this.dados()?.classificacao ?? null);

  /** Só contestação e petição simples entram na Fase 1 (corte #3, doc 04). */
  readonly podeRascunhar = computed(() => {
    const c = this.classificacao();
    return !!c && (c.tipoPecaSugerida === 'CONTESTACAO' || c.tipoPecaSugerida === 'PETICAO_SIMPLES');
  });

  readonly diasPulados = computed(
    () => this.prazo()?.passos.filter((p) => !p.contado).length ?? 0,
  );

  constructor() {
    // input() só está disponível após a inicialização — daí o microtask.
    queueMicrotask(() => this.carregar());
  }

  private carregar(): void {
    this.service.intimacao(this.id()).subscribe({
      next: (d) => {
        this.dados.set(d);
        this.rascunho.set(d.rascunho ?? '');
        this.prazo.set(d.prazo ?? null);

        // Recarregar a página no meio do processamento não pode perder o resultado.
        if (d.processando) {
          this.lendo.set(true);
          this.acompanhar();
        }
      },
      error: () => this.erro.set('Não encontrei essa intimação.'),
    });
  }

  /**
   * Dispara o fluxo de IA. Ele lê e, quando a peça está no escopo, já rascunha.
   *
   * <p>A resposta é só um 202: o trabalho leva minutos e acontece fora daqui. Quem traz o
   * resultado é o {@link acompanhar}.
   */
  ler(): void {
    this.lendo.set(true);
    this.erro.set(null);

    this.service.processar(this.id()).subscribe({
      next: () => this.acompanhar(),
      error: (e) => {
        this.erro.set(e?.error?.erro ?? 'Não consegui enviar a intimação para a IA.');
        this.lendo.set(false);
      },
    });
  }

  /**
   * Pergunta o estado a cada 3s até o fluxo terminar.
   *
   * <p>O teto de 100 tentativas (5 min) existe para a tela não girar para sempre se o
   * callback nunca chegar. Um spinner eterno é pior que uma mensagem de falha: ele não
   * diz ao advogado que ele precisa agir.
   */
  private acompanhar(): void {
    this.pararPolling();

    let tentativas = 0;
    this.polling = setInterval(() => {
      if (++tentativas > 100) {
        this.pararPolling();
        this.lendo.set(false);
        this.erro.set('A IA não respondeu a tempo. A intimação continua na fila — recarregue em instantes.');
        return;
      }

      this.service.intimacao(this.id()).subscribe({
        next: (d) => {
          this.dados.set(d);
          this.prazo.set(d.prazo ?? null);
          if (d.rascunho) this.rascunho.set(d.rascunho);

          if (!d.processando) {
            this.pararPolling();
            this.lendo.set(false);
            if (d.erroIa) this.erro.set(d.erroIa);
          }
        },
        error: () => {
          // Uma falha isolada de rede não derruba o acompanhamento; a próxima volta tenta.
        },
      });
    }, 3000);
  }

  private pararPolling(): void {
    if (this.polling !== null) {
      clearInterval(this.polling);
      this.polling = null;
    }
  }

  ngOnDestroy(): void {
    this.pararPolling();
  }

  /**
   * Exporta o que está no editor — o texto revisado pelo advogado, não o que a IA gerou.
   *
   * .txt por ora; o .docx formatado para protocolo é item da Fase 1 (doc 04, §3).
   */
  baixar(): void {
    const d = this.dados();
    if (!d) return;

    const blob = new Blob([this.rascunho()], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `rascunho-${d.processo.numero.replace(/[^\d]/g, '')}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  }

  rotuloPeca(tipo: string): string {
    switch (tipo) {
      case 'CONTESTACAO': return 'Contestação';
      case 'PETICAO_SIMPLES': return 'Petição simples';
      default: return 'Nenhuma peça no escopo da Fase 1';
    }
  }

  rotuloContagem(tipo: string): string {
    return tipo === 'DIAS_CORRIDOS' ? 'dias corridos' : 'dias úteis';
  }
}
