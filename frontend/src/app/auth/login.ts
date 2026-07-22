import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from './auth.service';

/**
 * A porta de entrada.
 *
 * <p>Um botão só. Sem senha para criar, esquecer ou vazar — e sem formulário de cadastro
 * entre o advogado e a primeira tela útil.
 *
 * <p>O link para a calculadora fica visível de propósito: ela é pública e é o ímã de
 * leads (doc 07). Quem chegou sem conta ainda tem para onde ir.
 */
@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private readonly auth = inject(AuthService);
  readonly entrando = signal(false);

  entrar(): void {
    this.entrando.set(true);
    this.auth.entrar();
  }
}
