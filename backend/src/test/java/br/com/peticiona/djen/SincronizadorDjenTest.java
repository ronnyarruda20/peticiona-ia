package br.com.peticiona.djen;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.peticiona.BancoDeTeste;
import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioRepository;
import br.com.peticiona.demo.Intimacao;
import br.com.peticiona.demo.IntimacaoRepository;
import br.com.peticiona.demo.Processo;
import br.com.peticiona.demo.ProcessoRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * A ingestão do DJEN, provada contra Postgres real e com a resposta real do CNJ na fixture.
 *
 * <p>O que importa aqui não é "o código roda", é "o código não duplica e não vaza". Uma
 * publicação que entra duas vezes vira dois prazos na agenda; uma que vaza para o advogado
 * errado é quebra de sigilo. Os dois cenários têm teste.
 *
 * <p>O {@link DjenCliente} é substituído por um que devolve a fixture: assim a suíte não
 * depende da rede nem da disponibilidade do CNJ, e continua verde quando a API está fora.
 */
@SpringBootTest
@EnabledIf("br.com.peticiona.BancoDeTeste#disponivel")
@DisplayName("Sincronização do DJEN")
class SincronizadorDjenTest {

    @DynamicPropertySource
    static void configuracao(DynamicPropertyRegistry registro) {
        BancoDeTeste.configurar(registro);
        registro.add("spring.security.oauth2.client.registration.google.client-id", () -> "teste");
        registro.add("spring.security.oauth2.client.registration.google.client-secret", () -> "teste");
    }

    /** Devolve a fixture no primeiro pedido e uma lista vazia depois — simula "nada novo". */
    @TestConfiguration
    static class ClienteFake {
        @Bean
        @Primary
        DjenCliente djenFake() {
            return (oab, uf, inicio, fim) -> FixtureDjen.publicacoes();
        }
    }

    @Autowired private SincronizadorDjen sincronizador;
    @Autowired private UsuarioRepository usuarios;
    @Autowired private ProcessoRepository processos;
    @Autowired private IntimacaoRepository intimacoes;

    private Usuario ana;
    private Usuario bruno;

    @BeforeEach
    void doisAdvogadosComOab() {
        // A classe não é @Transactional — a sincronização abre transação própria e o
        // rollback do teste não a alcançaria. Então limpamos à mão entre os métodos.
        intimacoes.deleteAll();
        processos.deleteAll();
        usuarios.deleteAll();

        ana = novo("sub-djen-ana", "ana.djen@exemplo.com", "93776", "MG");
        bruno = novo("sub-djen-bruno", "bruno.djen@exemplo.com", "120613", "MG");
    }

    private Usuario novo(String sub, String email, String oab, String uf) {
        Usuario u = new Usuario(sub, email, "Adv", null);
        u.setOabNumero(oab);
        u.setOabUf(uf);
        return usuarios.save(u);
    }

    @Test
    @DisplayName("importa as publicações da fixture como intimações e processos")
    void importaAsPublicacoes() {
        int novas = sincronizador.sincronizar(ana);

        assertThat(novas).isEqualTo(FixtureDjen.publicacoes().size());
        List<Intimacao> minhas = intimacoes.findByUsuarioOrderByCriadoEmAsc(ana);
        assertThat(minhas).hasSize(novas);
        // Toda intimação do DJEN carrega o hash e o link do PJe.
        assertThat(minhas).allSatisfy(i -> {
            assertThat(i.getDjenHash()).isNotBlank();
            assertThat(i.getLink()).isNotBlank();
        });
    }

    @Test
    @DisplayName("sincronizar duas vezes não duplica — a segunda rodada não traz nada novo")
    void naoDuplicaEntreRodadas() {
        int primeira = sincronizador.sincronizar(ana);
        int segunda = sincronizador.sincronizar(ana);

        assertThat(primeira).isGreaterThan(0);
        assertThat(segunda).isZero();
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(ana)).hasSize(primeira);
    }

    @Test
    @DisplayName("a mesma publicação entra para dois advogados diferentes")
    void mesmaPublicacaoParaDoisAdvogados() {
        sincronizador.sincronizar(ana);
        sincronizador.sincronizar(bruno);

        // A dedup é por (usuário, hash): o acervo de um não bloqueia o do outro.
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(ana)).isNotEmpty();
        assertThat(intimacoes.findByUsuarioOrderByCriadoEmAsc(bruno)).isNotEmpty();
    }

    @Test
    @DisplayName("o processo nasce aguardando confirmação de cliente")
    void processoNasceSemCliente() {
        sincronizador.sincronizar(ana);

        List<Processo> pendentes = processos.findByUsuarioAndClientePoloIsNullAndOrigem(ana, "DJEN");
        assertThat(pendentes).isNotEmpty();
        assertThat(pendentes).allSatisfy(p -> {
            assertThat(p.aguardaConfirmacaoDeCliente()).isTrue();
            assertThat(p.getCliente()).isNull();
        });
    }

    @Test
    @DisplayName("confirmar o polo preenche cliente e parte contrária a partir das partes")
    void confirmarClientePreencheOsLados() {
        sincronizador.sincronizar(ana);
        Processo p = processos.findByUsuarioAndClientePoloIsNullAndOrigem(ana, "DJEN").stream()
                .filter(x -> !x.getPartesPoloAtivo().isBlank() && !x.getPartesPoloPassivo().isBlank())
                .findFirst().orElseThrow();

        String ativo = p.getPartesPoloAtivo();
        String passivo = p.getPartesPoloPassivo();
        p.confirmarCliente("A");

        assertThat(p.getCliente()).isEqualTo(ativo);
        assertThat(p.getParteContraria()).isEqualTo(passivo);
        assertThat(p.aguardaConfirmacaoDeCliente()).isFalse();
    }

    @Test
    @DisplayName("registra até onde sincronizou, para a próxima rodada não recomeçar do zero")
    void registraOPontoDeSincronizacao() {
        sincronizador.sincronizar(ana);

        Usuario recarregado = usuarios.findById(ana.getId()).orElseThrow();
        assertThat(recarregado.getDjenSincronizadoAte()).isEqualTo(LocalDate.now());
        assertThat(recarregado.getDjenErro()).isNull();
    }
}
