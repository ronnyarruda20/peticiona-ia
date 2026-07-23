import { Component, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * Onde o advogado vê e mexe nos próprios dados.
 *
 * <p>Nome, foto e e-mail vêm do Google e são só leitura — mudam lá, não aqui. O que é
 * editável é a OAB (a chave das publicações) e a própria existência da conta.
 */
@Component({
  selector: 'app-perfil',
  imports: [FormsModule, RouterLink, DatePipe],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly usuario = this.auth.usuario;

  // ── OAB ───────────────────────────────────────────────────────
  readonly oabNumero = signal('');
  readonly oabUf = signal('');
  readonly salvandoOab = signal(false);
  readonly oabSalva = signal(false);

  // ── Sincronização ─────────────────────────────────────────────
  readonly sincronizando = signal(false);

  // ── Exclusão ──────────────────────────────────────────────────
  readonly confirmandoExclusao = signal(false);
  readonly emailDigitado = signal('');
  readonly excluindo = signal(false);

  readonly erro = signal<string | null>(null);

  /** Habilita o botão de excluir só quando o e-mail digitado bate exatamente. */
  readonly podeExcluir = computed(
    () => this.emailDigitado().trim().toLowerCase() === (this.usuario()?.email ?? '').toLowerCase(),
  );

  constructor() {
    // Preenche os campos com a OAB atual assim que o usuário estiver carregado.
    const u = this.usuario();
    if (u) {
      this.oabNumero.set(u.oabNumero ?? '');
      this.oabUf.set(u.oabUf ?? '');
    }
  }

  salvarOab(): void {
    const numero = this.oabNumero().trim();
    const uf = this.oabUf().trim().toUpperCase();
    if (!numero || uf.length !== 2) {
      this.erro.set('Informe o número da OAB e a UF com duas letras.');
      return;
    }
    this.salvandoOab.set(true);
    this.oabSalva.set(false);
    this.erro.set(null);
    this.auth.salvarOab(numero, uf).subscribe({
      next: () => {
        this.salvandoOab.set(false);
        this.oabSalva.set(true);
        // Trocar a OAB dispara nova busca no backend; releio para atualizar a data.
        this.auth.recarregar().subscribe();
      },
      error: (e) => {
        this.salvandoOab.set(false);
        this.erro.set(e?.error?.erro ?? 'Não consegui salvar a OAB.');
      },
    });
  }

  sincronizar(): void {
    this.sincronizando.set(true);
    this.erro.set(null);
    this.auth.sincronizarAgora().subscribe({
      next: () => {
        // A busca roda em segundo plano; releio depois de um instante para pegar a data nova.
        setTimeout(() => this.auth.recarregar().subscribe(() => this.sincronizando.set(false)), 4000);
      },
      error: (e) => {
        this.sincronizando.set(false);
        this.erro.set(e?.error?.erro ?? 'Não consegui iniciar a busca.');
      },
    });
  }

  excluir(): void {
    const email = this.usuario()?.email;
    if (!email || !this.podeExcluir()) return;
    this.excluindo.set(true);
    this.auth.excluirConta(email).subscribe({
      next: () => {
        this.auth.usuario.set(null);
        this.router.navigate(['/login']);
      },
      error: (e) => {
        this.excluindo.set(false);
        this.erro.set(e?.error?.erro ?? 'Não consegui excluir a conta.');
      },
    });
  }

  sair(evento: Event): void {
    evento.preventDefault();
    this.auth.sair();
  }
}
