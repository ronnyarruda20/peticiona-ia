import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * A única coisa que o produto precisa saber antes de trabalhar: a OAB do advogado.
 *
 * <p>É por ela que o DJEN encontra as publicações dele. Sem isto o "Seu dia" seria uma
 * tela vazia sem explicação — então quem entra sem OAB cadastrada passa por aqui primeiro.
 *
 * <p>Salvar dispara a busca de 30 dias no backend, em segundo plano. A tela não espera:
 * manda o advogado para o dashboard, que mostra o acervo chegando.
 */
@Component({
  selector: 'app-onboarding-oab',
  imports: [FormsModule],
  templateUrl: './onboarding-oab.html',
  styleUrl: './login.css',
})
export class OnboardingOab {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly numero = signal('');
  readonly uf = signal('');
  readonly salvando = signal(false);
  readonly erro = signal<string | null>(null);

  salvar(): void {
    const numero = this.numero().trim();
    const uf = this.uf().trim().toUpperCase();
    if (!numero || uf.length !== 2) {
      this.erro.set('Informe o número da OAB e a UF com duas letras (ex.: MG).');
      return;
    }

    this.salvando.set(true);
    this.erro.set(null);
    this.auth.salvarOab(numero, uf).subscribe({
      next: () => {
        // A carga roda em segundo plano; o dashboard mostra "buscando publicações".
        this.router.navigate(['/']);
      },
      error: (e) => {
        this.erro.set(e?.error?.erro ?? 'Não consegui salvar a OAB. Tente de novo.');
        this.salvando.set(false);
      },
    });
  }
}
