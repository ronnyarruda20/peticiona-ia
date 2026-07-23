package br.com.peticiona.demo;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.peticiona.BancoDeTeste;
import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

/**
 * A garantia central deste sistema: o acervo de um advogado não vaza para outro.
 *
 * <p>Este é o teste que justifica a existência dos outros. Um processo trabalhista contém
 * nome de cliente, valor de causa e estratégia de defesa; um vazamento aqui não é bug de
 * software, é quebra de sigilo profissional.
 *
 * <p>Roda contra Postgres de verdade, via Testcontainers, e não contra um banco em
 * memória: o isolamento depende de chave estrangeira e de migração do Flyway, e um H2
 * fingindo ser Postgres provaria menos do que parece.
 */
@SpringBootTest
@Transactional
@EnabledIf("br.com.peticiona.BancoDeTeste#disponivel")
@DisplayName("Isolamento do acervo por usuário")
class IsolamentoPorUsuarioTest {

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registro) {
        BancoDeTeste.configurar(registro);
        // O teste não passa pelo Google; sem isto o contexto exige as credenciais OAuth.
        registro.add("spring.security.oauth2.client.registration.google.client-id", () -> "teste");
        registro.add("spring.security.oauth2.client.registration.google.client-secret", () -> "teste");
    }

    @Autowired private UsuarioRepository usuarios;
    @Autowired private IntimacaoRepository intimacoes;
    @Autowired private AcervoDemo acervo;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void semearDoisAdvogados() {
        ana = usuarios.save(new Usuario("sub-ana", "ana@exemplo.com", "Ana", null));
        bruno = usuarios.save(new Usuario("sub-bruno", "bruno@exemplo.com", "Bruno", null));
        acervo.carregarExemplos(ana);
        acervo.carregarExemplos(bruno);
    }

    @Test
    @DisplayName("cada advogado enxerga apenas as próprias intimações")
    void listagemNaoMistura() {
        var deAna = intimacoes.findByUsuarioOrderByCriadoEmAsc(ana);
        var deBruno = intimacoes.findByUsuarioOrderByCriadoEmAsc(bruno);

        assertThat(deAna).hasSize(3);
        assertThat(deBruno).hasSize(3);
        // Mesmo conteúdo semeado, registros distintos: são cópias, não compartilhamento.
        assertThat(deAna).extracting(Intimacao::getId)
                .doesNotContainAnyElementsOf(deBruno.stream().map(Intimacao::getId).toList());
    }

    @Test
    @DisplayName("buscar pelo id de outro advogado devolve vazio, não o dado dele")
    void buscaPorIdRespeitaODono() {
        var doBruno = intimacoes.findByUsuarioOrderByCriadoEmAsc(bruno).get(0);

        // Ana tem o id em mãos — o que aconteceria se ela o adivinhasse ou o recebesse.
        assertThat(intimacoes.findByIdAndUsuario(doBruno.getId(), ana)).isEmpty();
        assertThat(intimacoes.findByIdAndUsuario(doBruno.getId(), bruno)).isPresent();
    }

    @Test
    @DisplayName("o que a IA escreve no acervo de um não aparece no do outro")
    void resultadoDaIaNaoVaza() {
        var deAna = intimacoes.findByUsuarioOrderByCriadoEmAsc(ana).get(0);
        deAna.setClassificacao(new ClassificacaoIntimacao(
                "Sentença", 8, "DIAS_UTEIS", "Recorrer", "PETICAO_SIMPLES", "ALTA", 0.95, "trecho"));
        deAna.setRascunho("Minuta confidencial da Ana.");
        intimacoes.saveAndFlush(deAna);

        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(bruno))
                .allSatisfy(i -> {
                    assertThat(i.getRascunho()).isNull();
                    assertThat(i.getClassificacao()).isNull();
                });
    }

    @Test
    @DisplayName("remover exemplos limpa só o acervo de quem pediu")
    void removerExemplosNaoAfetaOsOutros() {
        acervo.removerExemplos(ana);

        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(ana)).isEmpty();
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(bruno)).hasSize(3);
    }

    @Test
    @DisplayName("o teto diário de IA barra a partir da execução seguinte ao limite")
    void tetoDiarioBarra() {
        for (int i = 0; i < Usuario.LIMITE_DIARIO_IA; i++) {
            assertThat(ana.consumirExecucaoIa()).isTrue();
        }
        assertThat(ana.consumirExecucaoIa()).isFalse();
        // O teto é por usuário: o de Ana não gasta o de Bruno.
        assertThat(bruno.consumirExecucaoIa()).isTrue();
    }
}
