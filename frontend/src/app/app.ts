import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Casca da aplicação. Cada rota traz o próprio cabeçalho — a calculadora é uma
 * página pública de captação e o painel é o produto; misturar os dois num header
 * comum apagaria essa distinção.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: '<router-outlet />',
})
export class App {}
