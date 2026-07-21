package br.com.peticiona.ia;

import br.com.peticiona.demo.ClassificacaoIntimacao;
import br.com.peticiona.demo.Intimacao;
import br.com.peticiona.demo.Processo;
import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;

import com.anthropic.models.messages.StructuredMessageCreateParams;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * Lê uma publicação do DJEN e extrai o que importa: ato, prazo, providência, urgência.
 *
 * <p>Usa <b>structured outputs</b> — o schema sai do record {@link ClassificacaoIntimacao}
 * e a API garante que a resposta valida contra ele. Não existe parsing de texto livre neste
 * caminho; ou vem um objeto válido, ou a chamada falha alto.
 *
 * <p><b>O que este serviço deliberadamente NÃO faz:</b> calcular a data de vencimento. Ele
 * devolve o número de dias e o regime de contagem; a data sai do motor determinístico. Um
 * LLM que erra uma data por um dia perde um prazo — e prazo perdido é dano indenizável
 * (doc 09).
 */
@Service
public class LeitorIntimacao {

    /** Modelo fixo. Trocar de modelo muda o comportamento — é decisão consciente, não default. */
    private static final String MODELO = "claude-opus-4-8";

    private static final String INSTRUCOES = """
            Você lê publicações do Diário Eletrônico da Justiça do Trabalho brasileira e as \
            classifica para um advogado autônomo.

            O texto chega como vem do DJEN: caixa alta, sem acentuação, sem parágrafos. \
            Isso é normal — não é erro de transmissão.

            Regras que não se negociam:

            1. NUNCA calcule datas. Devolva apenas o número de dias do prazo e o regime de \
               contagem. A data-limite é calculada por outro componente, determinístico.
            2. O prazo é o que a publicação diz, não o que a lei diz em tese. Se o texto \
               fixa 5 dias, são 5 dias — mesmo que o prazo legal daquele ato seja outro.
            3. Se a publicação não abre prazo para o advogado (mero andamento, juntada de \
               terceiro, expediente de cartório), use prazoEmDias = 0 e \
               tipoPecaSugerida = NENHUMA.
            4. Prazos processuais correm em DIAS_UTEIS (CLT art. 775, CPC art. 219). \
               DIAS_CORRIDOS só quando a publicação disser expressamente, ou quando o prazo \
               for de direito material.
            5. A fundamentação deve ser um trecho LITERAL da publicação — é o que o advogado \
               vai reler para conferir sua leitura. Não parafraseie.
            6. Seja honesto na confiança. Publicação truncada, ambígua ou de matéria fora do \
               trabalhista merece confiança baixa. Confiança abaixo de 0,7 manda a intimação \
               para revisão humana, que é exatamente o que deve acontecer quando você não tem \
               certeza. Inflar confiança é o pior erro possível aqui.
            """;

    private final ObjectProvider<AnthropicClient> clientes;

    public LeitorIntimacao(ObjectProvider<AnthropicClient> clientes) {
        this.clientes = clientes;
    }

    public boolean disponivel() {
        return clientes.getIfAvailable() != null;
    }

    public ClassificacaoIntimacao classificar(Intimacao intimacao, Processo processo) {
        AnthropicClient client = clientes.getIfAvailable();
        if (client == null) {
            throw new IaIndisponivelException();
        }

        String pergunta = """
                Contexto do processo (para você entender do que se trata — não classifique isto):
                - Número: %s
                - Cliente do escritório: %s
                - Parte contrária: %s
                - Vara: %s
                - Fase: %s
                - Resumo: %s

                Publicação a classificar, órgão %s, publicada em %s:

                <publicacao>
                %s
                </publicacao>
                """.formatted(
                processo.numero(), processo.cliente(), processo.parteContraria(),
                processo.vara(), processo.fase(), processo.resumo(),
                intimacao.getOrgao(), intimacao.getDataPublicacao(), intimacao.getTexto());

        StructuredMessageCreateParams<ClassificacaoIntimacao> params = MessageCreateParams.builder()
                .model(MODELO)
                .maxTokens(4000L)
                .system(INSTRUCOES)
                .outputConfig(ClassificacaoIntimacao.class)
                .addUserMessage(pergunta)
                .build();

        return client.messages().create(params).content().stream()
                .flatMap(bloco -> bloco.text().stream())
                .map(texto -> texto.text())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "A API não devolveu uma classificação estruturada."));
    }
}
