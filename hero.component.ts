import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-hero',
  standalone: true,
  imports: [MatButtonModule],
  template: `
    <section class="hero">
      <div class="container">
        <h1 class="hero-title">Nunca mais perca um prazo.</h1>
        <p class="hero-subtitle">
          O Peticiona.IA monitora os diários oficiais, calcula seus prazos com precisão e
          rascunha suas peças. Para que você foque em advogar, não em gerenciar.
        </p>
        <a href="/register" mat-flat-button color="primary" class="hero-cta">
          Comece grátis e importe seus processos
        </a>
        <span class="hero-credit-card">Não precisa de cartão de crédito.</span>
      </div>
    </section>
  `,
  styles: [`
    :host {
      --surface-color: #f8f9fa;
      --text-primary-color: #212529;
      --text-secondary-color: #495057;
      --font-family-prose: "Source Serif 4", serif;
    }
    .container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }
    .hero { text-align: center; padding: 80px 24px; background-color: var(--surface-color); }
    .hero-title { font-family: var(--font-family-prose); font-size: 3.5rem; font-weight: 600; color: var(--text-primary-color); margin: 0 0 16px; line-height: 1.2; }
    .hero-subtitle { font-size: 1.25rem; color: var(--text-secondary-color); max-width: 700px; margin: 0 auto 32px; line-height: 1.6; }
    .hero-cta { padding: 12px 24px !important; height: auto !important; font-size: 1rem; }
    .hero-credit-card { display: block; margin-top: 12px; font-size: 0.9rem; color: var(--text-secondary-color); }

    @media (max-width: 600px) {
      .hero-title {
        font-size: 2.5rem; /* Título menor */
      }
      .hero-subtitle {
        font-size: 1.1rem;
      }
    }
  `]
})
export class HeroComponent {}