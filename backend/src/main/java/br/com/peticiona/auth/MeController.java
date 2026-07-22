package br.com.peticiona.auth;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Quem sou eu — é por aqui que o front decide entre mostrar a tela ou mandar para o login. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UsuarioAtual usuarioAtual;
    private final UsuarioRepository usuarios;

    public MeController(UsuarioAtual usuarioAtual, UsuarioRepository usuarios) {
        this.usuarioAtual = usuarioAtual;
        this.usuarios = usuarios;
    }

    @GetMapping
    public Map<String, Object> eu() {
        Usuario u = usuarioAtual.obrigatorio();
        Map<String, Object> corpo = new java.util.LinkedHashMap<>();
        corpo.put("email", u.getEmail());
        corpo.put("nome", u.getNome());
        corpo.put("fotoUrl", u.getFotoUrl());
        corpo.put("oabNumero", u.getOabNumero());
        corpo.put("oabUf", u.getOabUf());
        // O front mostra quanto ainda dá para usar hoje, em vez de deixar o usuário
        // descobrir o limite ao ser recusado.
        corpo.put("execucoesRestantesHoje", Usuario.LIMITE_DIARIO_IA - u.getExecucoesNoDia());
        return corpo;
    }

    /** A OAB do advogado — a chave que a ingestão do DJEN vai usar para achar o que é dele. */
    @PostMapping("/oab")
    public Map<String, Object> salvarOab(@RequestBody OabRequest req) {
        Usuario u = usuarioAtual.obrigatorio();
        u.setOabNumero(req.numero() == null ? null : req.numero().trim());
        u.setOabUf(req.uf() == null ? null : req.uf().trim().toUpperCase());
        usuarios.save(u);
        return Map.of("oabNumero", String.valueOf(u.getOabNumero()), "oabUf", String.valueOf(u.getOabUf()));
    }

    public record OabRequest(String numero, String uf) {}

    @ExceptionHandler(UsuarioAtual.NaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> naoAutenticado(UsuarioAtual.NaoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", e.getMessage()));
    }
}
