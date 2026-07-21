package br.com.peticiona.ia;

import br.com.peticiona.demo.ClassificacaoIntimacao;
import br.com.peticiona.demo.Intimacao;
import br.com.peticiona.demo.Processo;
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;

import com.anthropic.models.messages.OutputConfig;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Rascunha a peça de resposta a partir da intimação e dos dados do processo.
 *
 * <p>É o coração do produto (doc 04, §3) — e o ponto de maior risco de compliance (doc 09).
 * Duas travas estruturais:
 *
 * <ul>
 *   <li><b>Rascunho, nunca peça pronta.</b> O texto sai com lacunas explícitas onde o
 *       advogado precisa decidir. Uma peça que parece pronta é um convite a protocolar sem
 *       ler — exatamente o que não pode acontecer.</li>
 *   <li><b>Nada de jurisprudência inventada.</b> O modelo não cita número de acórdão, de
 *       súmula ou de precedente que não esteja na intimação. Citação alucinada em peça
 *       protocolada já rendeu multa por litigância de má-fé no Brasil.</li>
 * </ul>
 */
@Service
public class Redator {

    /** Mesmo modelo do leitor — ver {@link LeitorIntimacao#MODELO}. */
    private static final String MODELO = "claude-opus-4-8";

    private static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String INSTRUCOES = """
            Você rascunha peças trabalhistas para um advogado autônomo brasileiro revisar. \
            Você não é o advogado: você adianta o trabalho braçal para que ele gaste o tempo \
            dele decidindo tese, não formatando cabeçalho.

            Regras que não se negociam:

            1. NUNCA cite jurisprudência, súmula, orientação jurisprudencial ou precedente \
               que não esteja no texto da intimação. Nada de "STF, RE 1.234.567" inventado. \
               Onde uma tese pediria respaldo jurisprudencial, escreva a lacuna: \
               [INSERIR PRECEDENTE — pesquisar e conferir]. Citar acórdão que não existe é \
               litigância de má-fé.
            2. Artigos de lei você pode citar, desde que tenha certeza do conteúdo. Na dúvida, \
               descreva a regra sem o número.
            3. Todo fato que você não tem nos autos vira lacuna explícita em colchetes \
               maiúsculos: [CONFERIR DATA DE ADMISSÃO], [ANEXAR CARTÕES DE PONTO]. Não invente \
               fato, valor, data ou nome. Não "preencha o vazio com o plausível".
            4. NUNCA escreva a data de protocolo nem a data-limite do prazo. Essas vêm do \
               sistema.
            5. Português jurídico correto e sóbrio. Sem adjetivo inflado, sem "data venia" a \
               cada parágrafo, sem retórica. Argumento seco convence mais.
            6. Estruture com títulos em caixa alta (DOS FATOS, DO DIREITO, DOS PEDIDOS), na \
               ordem usual da peça. Use texto corrido — nada de markdown, tabelas ou bullets \
               decorativos; isto vai virar um .docx de protocolo.
            7. Encerre com uma seção final "— PONTOS PARA O ADVOGADO REVISAR —" listando o que \
               você deixou em aberto e por quê. Essa seção existe para ser lida e apagada \
               antes do protocolo.
            """;

    private final ObjectProvider<AnthropicClient> clientes;

    public Redator(ObjectProvider<AnthropicClient> clientes) {
        this.clientes = clientes;
    }

    public String rascunhar(Intimacao intimacao, Processo processo, LocalDate vencimento) {
        AnthropicClient client = clientes.getIfAvailable();
        if (client == null) {
            throw new IaIndisponivelException();
        }

        ClassificacaoIntimacao c = intimacao.getClassificacao();
        if (c == null) {
            throw new IllegalStateException("Classifique a intimação antes de rascunhar a peça.");
        }

        String peca = switch (c.tipoPecaSugerida()) {
            case "CONTESTACAO" -> "uma CONTESTAÇÃO trabalhista";
            case "PETICAO_SIMPLES" -> "uma PETIÇÃO SIMPLES atendendo ao que foi determinado";
            default -> throw new IllegalStateException(
                    "Esta intimação não pede peça no escopo da Fase 1 (contestação ou petição simples).");
        };

        String pergunta = """
                Rascunhe %s.

                PROCESSO
                - Número: %s
                - Vara: %s
                - Nosso cliente: %s
                - Parte contrária: %s
                - Fase: %s
                - Resumo dos autos: %s

                O QUE FOI PUBLICADO
                - Ato: %s
                - Providência: %s
                - Prazo concedido: %d %s, vencendo em %s (já calculado pelo sistema — não repita esta data na peça)

                TEXTO INTEGRAL DA PUBLICAÇÃO
                <publicacao>
                %s
                </publicacao>
                """.formatted(
                peca,
                processo.numero(), processo.vara(), processo.cliente(),
                processo.parteContraria(), processo.fase(), processo.resumo(),
                c.tipoAto(), c.providencia(),
                c.prazoEmDias(),
                "DIAS_UTEIS".equals(c.tipoContagem()) ? "dias úteis" : "dias corridos",
                vencimento.format(BR),
                intimacao.getTexto());

        MessageCreateParams params = MessageCreateParams.builder()
                .model(MODELO)
                .maxTokens(8000L)
                .system(INSTRUCOES)
                .thinking(ThinkingConfigAdaptive.builder().build())
                .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.HIGH).build())
                .addUserMessage(pergunta)
                .build();

        StringBuilder texto = new StringBuilder();
        client.messages().create(params).content().stream()
                .flatMap(bloco -> bloco.text().stream())
                .forEach(bloco -> texto.append(bloco.text()));

        if (texto.isEmpty()) {
            throw new IllegalStateException("A API não devolveu texto para o rascunho.");
        }
        return texto.toString();
    }
}
