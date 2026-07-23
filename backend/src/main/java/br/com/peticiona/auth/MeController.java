package br.com.peticiona.auth;

import br.com.peticiona.demo.IntimacaoRepository;
import br.com.peticiona.demo.ProcessoRepository;
import br.com.peticiona.djen.RotinaDjen;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Quem sou eu — é por aqui que o front decide entre mostrar a tela ou mandar para o login. */
@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UsuarioAtual usuarioAtual;
    private final UsuarioRepository usuarios;
    private final ProcessoRepository processos;
    private final IntimacaoRepository intimacoes;
    private final RotinaDjen rotina;

    public MeController(UsuarioAtual usuarioAtual, UsuarioRepository usuarios,
                        ProcessoRepository processos, IntimacaoRepository intimacoes,
                        RotinaDjen rotina) {
        this.usuarioAtual = usuarioAtual;
        this.usuarios = usuarios;
        this.processos = processos;
        this.intimacoes = intimacoes;
        this.rotina = rotina;
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
        // Acervo desatualizado parece completo — por isso a tela precisa poder avisar.
        corpo.put("djenSincronizadoAte",
                u.getDjenSincronizadoAte() == null ? null : u.getDjenSincronizadoAte().toString());
        corpo.put("djenUltimaSincronizacao",
                u.getDjenUltimaSincronizacao() == null ? null : u.getDjenUltimaSincronizacao().toString());
        corpo.put("djenErro", u.getDjenErro());
        return corpo;
    }

    /**
     * A OAB do advogado — a chave com que o DJEN encontra as publicações dele.
     *
     * <p>Salvar dispara a primeira carga, de trinta dias, em segundo plano: são centenas de
     * publicações e a tela não pode ficar travada esperando. O front acompanha pelo
     * dashboard, que mostra quando a última sincronização aconteceu.
     */
    @PostMapping("/oab")
    public Map<String, Object> salvarOab(@RequestBody OabRequest req) {
        Usuario u = usuarioAtual.obrigatorio();

        String numero = req.numero() == null ? null : req.numero().trim();
        String uf = req.uf() == null ? null : req.uf().trim().toUpperCase();
        if (numero == null || numero.isBlank() || uf == null || uf.length() != 2) {
            throw new IllegalArgumentException("Informe o número da OAB e a UF com duas letras.");
        }

        boolean mudou = !numero.equals(u.getOabNumero()) || !uf.equals(u.getOabUf());
        u.setOabNumero(numero);
        u.setOabUf(uf);
        usuarios.save(u);

        if (mudou) {
            rotina.cargaInicial(u);
        }

        return Map.of("oabNumero", numero, "oabUf", uf, "buscandoPublicacoes", mudou);
    }

    public record OabRequest(String numero, String uf) {}

    /**
     * Busca as publicações agora, sem esperar a rotina das 8h.
     *
     * <p>Útil logo depois de cadastrar a OAB ou quando chega intimação no meio do dia. Roda
     * assíncrono (a busca leva segundos ou minutos) e responde {@code 202}; a tela acompanha
     * pelo {@code djenUltimaSincronizacao} do {@code GET /api/me}.
     */
    @PostMapping("/sincronizar")
    public ResponseEntity<Map<String, Object>> sincronizarAgora() {
        Usuario u = usuarioAtual.obrigatorio();
        if (!u.temOab()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Cadastre sua OAB antes de buscar publicações."));
        }
        rotina.cargaInicial(u);
        return ResponseEntity.accepted().body(Map.of("status", "buscando"));
    }

    /**
     * Exclui a conta e todo o acervo — o direito de exclusão da LGPD.
     *
     * <p>Irreversível, então protegido contra o clique acidental: só executa quando o
     * parâmetro {@code confirmacao} bate com o e-mail da conta, no mesmo espírito do GitHub.
     *
     * <p>As FKs têm {@code ON DELETE CASCADE}, mas as limpezas são explícitas mesmo assim —
     * a exclusão de dado de cliente não deve depender de um detalhe de configuração do banco
     * para acontecer por inteiro.
     */
    @DeleteMapping
    @Transactional
    public ResponseEntity<Map<String, String>> excluirConta(
            @RequestParam(required = false) String confirmacao, HttpServletRequest req) {
        Usuario u = usuarioAtual.obrigatorio();

        if (confirmacao == null || !confirmacao.equalsIgnoreCase(u.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "erro", "Digite seu e-mail para confirmar a exclusão."));
        }

        intimacoes.deleteByUsuario(u);
        processos.deleteByUsuario(u);
        usuarios.delete(u);

        // Sem sessão não sobra rastro de login apontando para um usuário que não existe mais.
        req.getSession().invalidate();

        return ResponseEntity.ok(Map.of("status", "conta excluída"));
    }

    @ExceptionHandler(UsuarioAtual.NaoAutenticadoException.class)
    public ResponseEntity<Map<String, String>> naoAutenticado(UsuarioAtual.NaoAutenticadoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> entradaInvalida(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }
}
