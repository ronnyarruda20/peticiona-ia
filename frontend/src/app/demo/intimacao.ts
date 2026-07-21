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
  readonly rascunhando = signal(false);
  readonly erro = signal<string | null>(null);

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
      },
      error: () => this.erro.set('Não encontrei essa intimação.'),
    });
  }

  ler(): void {
    this.lendo.set(true);
    this.erro.set(null);

    this.service.classificar(this.id()).subscribe({
      next: (r) => {
        this.dados.update((d) => (d ? { ...d, classificacao: r.classificacao } : d));
        this.prazo.set(r.prazo ?? null);
        this.lendo.set(false);
      },
      error: (e) => {
        this.erro.set(e?.error?.erro ?? 'A leitura falhou. Tente de novo.');
        this.lendo.set(false);
      },
    });
  }

  rascunhar(): void {
    this.rascunhando.set(true);
    this.erro.set(null);

    this.service.rascunhar(this.id()).subscribe({
      next: (r) => {
        this.rascunho.set(r.rascunho);
        this.rascunhando.set(false);
      },
      error: (e) => {
        this.erro.set(e?.error?.erro ?? 'A redação falhou. Tente de novo.');
        this.rascunhando.set(false);
      },
    });
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
