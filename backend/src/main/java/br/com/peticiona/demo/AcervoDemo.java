package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Semeia o acervo de demonstração de um usuário recém-chegado.
 *
 * <p><b>Isto não é o produto.</b> Na Fase 1 os processos vêm do DataJud/CNJ pela OAB e as
 * publicações do DJEN/Comunica (doc 05) — a API pública do DJEN já foi validada. Aqui eles
 * são fixos para que a apresentação seja reprodutível: mesma tela, mesmos casos, toda vez.
 *
 * <p>O nicho é <b>trabalhista</b> — a hipótese da Fase 1 (doc 04, §3), ainda a confirmar
 * nas entrevistas.
 *
 * <p>Antes esta classe <i>era</i> o acervo, num mapa em memória compartilhado por todos os
 * visitantes ao mesmo tempo. Agora ela só escreve as linhas iniciais no banco, uma cópia
 * para cada advogado: o que um faz com a IA não aparece — nem some — para o outro.
 */
@Service
public class AcervoDemo {

    private final ProcessoRepository processos;
    private final IntimacaoRepository intimacoes;

    public AcervoDemo(ProcessoRepository processos, IntimacaoRepository intimacoes) {
        this.processos = processos;
        this.intimacoes = intimacoes;
    }

    /**
     * Carrega os três casos de exemplo — o botão "Carregar exemplos" da apresentação.
     *
     * <p>Idempotente e cirúrgico: só mexe em dados de origem {@code DEMO}. As publicações
     * reais do DJEN <b>não são tocadas</b>. Chamar duas vezes não duplica; chamar com
     * exemplos já presentes não faz nada.
     */
    @Transactional
    public void carregarExemplos(Usuario dono) {
        if (processos.findByUsuarioAndOrigem(dono, "DEMO").isEmpty()) {
            semear(dono);
        }
    }

    /**
     * Remove os exemplos, e só os exemplos.
     *
     * <p>Apaga apenas o que tem origem {@code DEMO} — o acervo real do advogado permanece
     * intacto. É o que permite manter os exemplos como recurso de demonstração sem risco de
     * levar junto um processo de cliente.
     */
    @Transactional
    public void removerExemplos(Usuario dono) {
        // Intimações primeiro: elas referenciam processos.
        intimacoes.deleteByUsuarioAndProcessoOrigem(dono, "DEMO");
        processos.deleteByUsuarioAndOrigem(dono, "DEMO");
    }

    private void semear(Usuario dono) {
        // Datas relativas a hoje: a demo nunca "envelhece" e os prazos ficam sempre críveis.
        LocalDate hoje = LocalDate.now();

        Processo p1 = processos.save(new Processo(
                dono, "0010547-32.2026.5.03.0114", "Marcos Aurélio Ferreira",
                "Transportes Cordilheira Ltda.", "14ª Vara do Trabalho de Belo Horizonte/MG",
                "Trabalhista", "Conhecimento — aguardando defesa",
                "Reclamação trabalhista: horas extras, adicional de periculosidade e verbas rescisórias. "
                        + "Motorista carreteiro, admitido em 03/2021, dispensado sem justa causa em 11/2025. "
                        + "Jornada alegada de 12h com 40min de intervalo. Valor da causa: R$ 148.320,00."));

        Processo p2 = processos.save(new Processo(
                dono, "0021118-76.2025.5.03.0026", "Cleusa Maria dos Santos",
                "Rede Bonavita Supermercados S/A", "26ª Vara do Trabalho de Belo Horizonte/MG",
                "Trabalhista", "Conhecimento — instrução encerrada",
                "Operadora de caixa. Pedidos de acúmulo de função, intervalo intrajornada suprimido e "
                        + "indenização por dano moral (revista íntima). Audiência de instrução realizada, "
                        + "razões finais remissivas. Valor da causa: R$ 62.400,00."));

        Processo p3 = processos.save(new Processo(
                dono, "0007733-05.2026.5.03.0009", "Wesley Nogueira Prado",
                "Construtora Vale Aurora Ltda.", "9ª Vara do Trabalho de Belo Horizonte/MG",
                "Trabalhista", "Conhecimento — saneamento",
                "Pedreiro. Vínculo negado pela reclamada, que alega contrato de empreitada. "
                        + "Discussão central é o reconhecimento do vínculo (CLT art. 3º). "
                        + "Valor da causa: R$ 91.750,00."));

        // ── Publicações ──────────────────────────────────────────────
        // Texto no formato bruto do DJEN: caixa alta, sem parágrafos, juridiquês corrido.
        // É esse ruído que a IA precisa atravessar — publicação "limpa" não prova nada.

        intimacoes.save(new Intimacao(
                dono, p1, hoje.minusDays(1),
                "TRT da 3ª Região — 14ª Vara do Trabalho de Belo Horizonte",
                "PODER JUDICIARIO JUSTICA DO TRABALHO TRIBUNAL REGIONAL DO TRABALHO DA 3A REGIAO "
                        + "14A VARA DO TRABALHO DE BELO HORIZONTE ATOrd 0010547-32.2026.5.03.0114 "
                        + "RECLAMANTE: MARCOS AURELIO FERREIRA RECLAMADO: TRANSPORTES CORDILHEIRA LTDA "
                        + "INTIMACAO Fica a parte reclamada NOTIFICADA da presente reclamatoria e INTIMADA "
                        + "para, querendo, apresentar defesa escrita no prazo de 15 (quinze) dias, sob pena "
                        + "de revelia e confissao quanto a materia de fato, nos termos do art. 847 da CLT c/c "
                        + "art. 335 do CPC. Fica ciente de que a contestacao devera vir acompanhada de todos "
                        + "os documentos e da indicacao das provas que pretende produzir. BELO HORIZONTE/MG, "
                        + "conforme data da assinatura eletronica."));

        intimacoes.save(new Intimacao(
                dono, p2, hoje.minusDays(2),
                "TRT da 3ª Região — 26ª Vara do Trabalho de Belo Horizonte",
                "PODER JUDICIARIO JUSTICA DO TRABALHO TRT DA 3A REGIAO 26A VARA DO TRABALHO DE BELO "
                        + "HORIZONTE ATOrd 0021118-76.2025.5.03.0026 RECLAMANTE: CLEUSA MARIA DOS SANTOS "
                        + "RECLAMADO: REDE BONAVITA SUPERMERCADOS S/A SENTENCA Isto posto, julgo PARCIALMENTE "
                        + "PROCEDENTES os pedidos formulados na inicial para condenar a reclamada ao pagamento "
                        + "de intervalo intrajornada suprimido, com adicional de 50%, e indenizacao por dano "
                        + "moral arbitrada em R$ 8.000,00. Improcedente o pedido de acumulo de funcao. Custas "
                        + "pela reclamada. Intimem-se as partes. Ficam as partes cientes de que o prazo para "
                        + "interposicao de recurso ordinario e de 8 (oito) dias, contados na forma do art. 775 "
                        + "da CLT."));

        intimacoes.save(new Intimacao(
                dono, p3, hoje,
                "TRT da 3ª Região — 9ª Vara do Trabalho de Belo Horizonte",
                "PODER JUDICIARIO JUSTICA DO TRABALHO TRT DA 3A REGIAO 9A VARA DO TRABALHO DE BELO "
                        + "HORIZONTE ATOrd 0007733-05.2026.5.03.0009 RECLAMANTE: WESLEY NOGUEIRA PRADO "
                        + "RECLAMADO: CONSTRUTORA VALE AURORA LTDA DESPACHO Vistos. Junte a reclamada, no "
                        + "prazo de 5 (cinco) dias, os contratos de empreitada mencionados em sua defesa, bem "
                        + "como os comprovantes de pagamento correspondentes ao periodo de 02/2024 a 09/2025. "
                        + "Apos, vista a parte reclamante. Intimem-se."));
    }
}
