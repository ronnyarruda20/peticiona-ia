# public/

Arquivos estáticos servidos na raiz do site.

O build copia tudo daqui para `dist/peticiona-web/browser/`, então um arquivo
`public/img/hero.webp` fica acessível em `/img/hero.webp` — é assim que se
referencia no template:

```html
<img src="/img/hero.webp" alt="" />
```

Configurado em `angular.json` (`assets: [{ "glob": "**/*", "input": "public" }]`).
