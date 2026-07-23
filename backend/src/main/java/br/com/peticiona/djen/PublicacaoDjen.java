package br.com.peticiona.djen;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uma comunicação como o CNJ a devolve.
 *
 * <p>Espelha só o que usamos: a API retorna mais de vinte campos, e mapear todos criaria
 * uma superfície de manutenção sem contrapartida. {@code @JsonIgnoreProperties} deixa o
 * resto passar — quando o CNJ acrescentar um campo, nada quebra.
 *
 * @param hash            chave de deduplicação, estável por comunicação
 * @param texto           a publicação em formato bruto: é o que a IA vai ler
 * @param dataDisponibilizacao data-base do prazo, a única entrada de data que importa
 * @param ativo           publicação cancelada vem com {@code false}
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicacaoDjen(

        Long id,
        String hash,
        String texto,

        @JsonProperty("data_disponibilizacao")
        LocalDate dataDisponibilizacao,

        @JsonProperty("numero_processo")
        String numeroProcesso,

        @JsonProperty("numeroprocessocommascara")
        String numeroProcessoComMascara,

        @JsonProperty("nomeOrgao")
        String nomeOrgao,

        @JsonProperty("siglaTribunal")
        String siglaTribunal,

        @JsonProperty("nomeClasse")
        String nomeClasse,

        @JsonProperty("tipoComunicacao")
        String tipoComunicacao,

        String link,
        Boolean ativo,

        @JsonProperty("motivo_cancelamento")
        String motivoCancelamento,

        List<Destinatario> destinatarios
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Destinatario(String nome, String polo) {}

    /**
     * Vale trazer para o acervo?
     *
     * <p>Publicação cancelada ou sem texto não é trabalho — é ruído que ocuparia lugar na
     * agenda do advogado e ainda gastaria uma execução de IA se ele clicasse.
     */
    public boolean aproveitavel() {
        return !Boolean.FALSE.equals(ativo)
                && (motivoCancelamento == null || motivoCancelamento.isBlank())
                && texto != null && !texto.isBlank()
                && hash != null && !hash.isBlank()
                && dataDisponibilizacao != null;
    }

    /** As partes de um polo, em uma linha só — é assim que a tela pergunta e o prompt lê. */
    public String partesDoPolo(String polo) {
        if (destinatarios == null) {
            return "";
        }
        return destinatarios.stream()
                .filter(d -> polo.equalsIgnoreCase(d.polo()))
                .map(Destinatario::nome)
                .filter(n -> n != null && !n.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    public List<Destinatario> destinatariosOuVazio() {
        return destinatarios == null ? new ArrayList<>() : destinatarios;
    }

    /** O número com máscara nem sempre vem; sem ele o cru serve para exibir. */
    public String numeroParaExibir() {
        return numeroProcessoComMascara != null && !numeroProcessoComMascara.isBlank()
                ? numeroProcessoComMascara
                : numeroProcesso;
    }
}
