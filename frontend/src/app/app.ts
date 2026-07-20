import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { PrazoService } from './prazo.service';
import { InteresseService } from './interesse.service';
import { Justica, ResultadoPrazo, TipoContagem } from './prazo';

/**
 * Calculadora de prazos.
 *
 * Princípios do doc 10 aplicados aqui:
 *  - "Prazo é sagrado": o resultado nunca aparece sem a memória de cálculo ao lado.
 *  - Limites explícitos: o que o motor NÃO cobre fica visível, não escondido em rodapé.
 *  - Densidade alta: advogado lê muito texto e quer a informação toda na tela.
 */
@Component({
  selector: 'app-root',
  imports: [FormsModule, DatePipe],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly service = inject(PrazoService);
  private readonly interesseService = inject(InteresseService);

  readonly dataIntimacao = signal(this.hoje());
  readonly prazoEmDias = signal(15);
  readonly tipoContagem = signal<TipoContagem>('DIAS_UTEIS');
  readonly justica = signal<Justica>('TRABALHISTA');
  readonly considerarRecesso = signal(true);

  readonly resultado = signal<ResultadoPrazo | null>(null);
  readonly erro = signal<string | null>(null);
  readonly carregando = signal(false);
  readonly memoriaAberta = signal(false);
  readonly interesseEnviado = signal(false);
  readonly interesseEnviando = signal(false);
  readonly interesseMensagem = signal<string | null>(null);
  readonly nome = signal('');
  readonly email = signal('');
  readonly interesse = signal('Conteúdo');
  readonly mensagem = signal('');

  /** Vazio = sem pesquisa configurada; o convite não aparece. */
  readonly pesquisaUrl = signal('');

  constructor() {
    this.service.config().subscribe({
      next: (c) => this.pesquisaUrl.set(c.pesquisaUrl ?? ''),
      error: () => this.pesquisaUrl.set(''),
    });
  }

  readonly diasNaoContados = computed(
    () => this.resultado()?.passos.filter((p) => !p.contado).length ?? 0,
  );

  readonly prazosComuns = [
    { label: 'Contestação (15 dias)', dias: 15 },
    { label: 'Recurso ordinário (8 dias)', dias: 8 },
    { label: 'Embargos de declaração (5 dias)', dias: 5 },
    { label: 'Manifestação (10 dias)', dias: 10 },
  ];

  calcular(): void {
    this.carregando.set(true);
    this.erro.set(null);

    this.service
      .calcular({
        dataIntimacao: this.dataIntimacao(),
        prazoEmDias: Number(this.prazoEmDias()),
        tipoContagem: this.tipoContagem(),
        justica: this.justica(),
        considerarRecesso: this.considerarRecesso(),
      })
      .subscribe({
        next: (r) => {
          this.resultado.set(r);
          this.carregando.set(false);
        },
        error: (e) => {
          this.erro.set(e?.error?.erro ?? 'Não consegui calcular. Confira os dados e tente de novo.');
          this.resultado.set(null);
          this.carregando.set(false);
        },
      });
  }

  usarPrazo(dias: number): void {
    this.prazoEmDias.set(dias);
    this.calcular();
  }

  alternarMemoria(): void {
    this.memoriaAberta.update((v) => !v);
  }

  enviarInteresse(): void {
    if (!this.nome() || !this.email()) {
      this.interesseMensagem.set('Informe nome e e-mail para receber o update.');
      return;
    }

    this.interesseEnviando.set(true);
    this.interesseMensagem.set(null);

    this.interesseService.registrar({
      nome: this.nome(),
      email: this.email(),
      interesse: this.interesse(),
      mensagem: this.mensagem(),
    }).subscribe({
      next: () => {
        this.interesseEnviado.set(true);
        this.interesseEnviando.set(false);
        this.interesseMensagem.set('Obrigado! Vamos te manter atualizado sobre o projeto.');
      },
      error: () => {
        this.interesseEnviando.set(false);
        this.interesseMensagem.set('Não foi possível registrar o interesse agora. Tente novamente mais tarde.');
      },
    });
  }

  private hoje(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
