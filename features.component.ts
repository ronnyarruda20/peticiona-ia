import { Component } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <section id="features" class="features">
      <div class="container">
        <div class="feature-card">
          <mat-icon>alarm_on</mat-icon>
          <h3>Prazos sob controle absoluto</h3>
          <p>
            Lemos o DJEN e outras fontes por você. Intimações com prazo são classificadas,
            e as datas-limite calculadas com base nas regras do tribunal e feriados locais.
          </p>
        </div>
        <div class="feature-card">
          <mat-icon>edit_document</mat-icon>
          <h3>Peças rascunhadas em minutos</h3>
          <p>
            Nossa IA gera a primeira versão de contestações e petições simples, já conectada
            aos dados do processo. Você revisa e edita, economizando horas.
          </p>
        </div>
        <div class="feature-card">
          <mat-icon>wb_sunny</mat-icon>
          <h3>Seu dia, organizado</h3>
          <p>
            Toda manhã, seu painel mostra o que é crítico: os prazos que vencem, as intimações
            que chegaram e os rascunhos que precisam da sua atenção.
          </p>
        </div>
      </div>
    </section>
  `,
  styles: [`
    :host { --brand-color: #004d40; --surface-raised-color: #ffffff; --text-secondary-color: #495057; }
    .container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }
    .features { padding: 64px 24px; background-color: var(--surface-raised-color); }
    .features .container { display: grid; grid-template-columns: repeat(3, 1fr); gap: 40px; text-align: center; }
    .feature-card mat-icon { font-size: 36px; height: 36px; width: 36px; color: var(--brand-color); }
    .feature-card h3 { font-size: 1.2rem; margin: 16px 0 8px; }
    .feature-card p { color: var(--text-secondary-color); line-height: 1.6; }

    @media (max-width: 960px) { .features .container { grid-template-columns: 1fr; gap: 48px; } }
  `]
})
export class FeaturesComponent {}