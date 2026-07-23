import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule],
  template: `
    <header class="header">
      <div class="container">
        <div class="logo">Peticiona.IA</div>
        <nav class="nav desktop-only">
          <a href="#features">Funcionalidades</a>
          <a href="#pricing">Preços</a>
          <a href="#">Blog</a>
        </nav>
        <div class="actions desktop-only">
          <a href="/login" mat-button>Entrar</a>
          <a href="/register" mat-flat-button color="primary">Comece grátis por 7 dias</a>
        </div>
        <button mat-icon-button class="mobile-only menu-button">
          <mat-icon>menu</mat-icon>
        </button>
      </div>
    </header>
  `,
  styles: [`
    :host {
      --brand-color: #004d40;
      --text-primary-color: #212529;
      --text-secondary-color: #495057;
      --surface-raised-color: #ffffff;
      --border-color: #dee2e6;
    }
    .container { max-width: 1100px; margin: 0 auto; padding: 0 24px; }
    .header { background-color: var(--surface-raised-color); border-bottom: 1px solid var(--border-color); padding: 16px 0; position: sticky; top: 0; z-index: 10; }
    .header .container { display: flex; align-items: center; justify-content: space-between; }
    .logo { font-weight: 600; font-size: 1.2rem; color: var(--brand-color); }
    .nav { display: flex; gap: 24px; }
    .nav a { text-decoration: none; color: var(--text-secondary-color); font-weight: 500; transition: color 0.2s; }
    .nav a:hover { color: var(--text-primary-color); }
    .actions { display: flex; align-items: center; gap: 8px; }

    .mobile-only {
      display: none;
    }

    @media (max-width: 960px) {
      .desktop-only {
        display: none;
      }
      .mobile-only {
        display: block;
      }
    }

    /* This is needed to make the primary button color work inside the component */
    ::ng-deep .mat-flat-button.mat-primary {
      background-color: var(--brand-color);
    }
  `]
})
export class HeaderComponent {}