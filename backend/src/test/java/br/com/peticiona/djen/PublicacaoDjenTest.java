package br.com.peticiona.djen;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O mapeamento da publicação real do CNJ, verificado contra a fixture capturada da API.
 *
 * <p>É um teste puro, sem Spring nem banco: só o JSON entrando e o record saindo. Se o CNJ
 * mudar o formato, é aqui que quebra primeiro — antes de qualquer coisa chegar ao acervo
 * de alguém.
 */
@DisplayName("Mapeamento da publicação do DJEN")
class PublicacaoDjenTest {

    @Test
    @DisplayName("os campos essenciais são lidos do JSON real")
    void leOsCamposEssenciais() {
        PublicacaoDjen p = FixtureDjen.publicacoes().get(0);

        assertThat(p.hash()).isNotBlank();
        assertThat(p.texto()).contains("JUSTIÇA DO TRABALHO");
        assertThat(p.dataDisponibilizacao()).isNotNull();
        assertThat(p.numeroProcesso()).isNotBlank();
        // O número com máscara precisa vir preenchido — é o que a tela mostra.
        assertThat(p.numeroParaExibir()).contains("-").contains(".");
        assertThat(p.aproveitavel()).isTrue();
    }

    @Test
    @DisplayName("as partes são agrupadas por polo, sem repetição")
    void agrupaPartesPorPolo() {
        PublicacaoDjen p = FixtureDjen.publicacoes().get(0);

        String ativo = p.partesDoPolo("A");
        String passivo = p.partesDoPolo("P");

        // Ao menos um dos polos tem parte; um deles pode estar vazio numa publicação real.
        assertThat(ativo + passivo).isNotBlank();
        // Sem duplicados: a mesma parte não pode aparecer duas vezes na linha.
        for (String lista : List.of(ativo, passivo)) {
            if (!lista.isBlank()) {
                String[] nomes = lista.split(", ");
                assertThat(nomes).doesNotHaveDuplicates();
            }
        }
    }

    @Test
    @DisplayName("publicação cancelada não é aproveitável")
    void canceladaNaoEntra() {
        PublicacaoDjen viva = FixtureDjen.publicacoes().get(0);
        PublicacaoDjen cancelada = new PublicacaoDjen(
                viva.id(), viva.hash(), viva.texto(), viva.dataDisponibilizacao(),
                viva.numeroProcesso(), viva.numeroProcessoComMascara(), viva.nomeOrgao(),
                viva.siglaTribunal(), viva.nomeClasse(), viva.tipoComunicacao(), viva.link(),
                false, "Cancelada por erro material", viva.destinatarios());

        assertThat(cancelada.aproveitavel()).isFalse();
    }

    @Test
    @DisplayName("publicação sem texto ou sem hash não é aproveitável")
    void semTextoOuHashNaoEntra() {
        PublicacaoDjen base = FixtureDjen.publicacoes().get(0);

        PublicacaoDjen semTexto = new PublicacaoDjen(
                base.id(), base.hash(), "  ", base.dataDisponibilizacao(),
                base.numeroProcesso(), base.numeroProcessoComMascara(), base.nomeOrgao(),
                base.siglaTribunal(), base.nomeClasse(), base.tipoComunicacao(), base.link(),
                true, null, base.destinatarios());
        assertThat(semTexto.aproveitavel()).isFalse();

        PublicacaoDjen semHash = new PublicacaoDjen(
                base.id(), null, base.texto(), base.dataDisponibilizacao(),
                base.numeroProcesso(), base.numeroProcessoComMascara(), base.nomeOrgao(),
                base.siglaTribunal(), base.nomeClasse(), base.tipoComunicacao(), base.link(),
                true, null, base.destinatarios());
        assertThat(semHash.aproveitavel()).isFalse();
    }
}
