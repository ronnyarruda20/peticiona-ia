package br.com.peticiona.prazo;

import java.time.LocalDate;
import java.util.List;

/**
 * O resultado do cálculo, <b>com a memória completa</b>.
 *
 * <p>A memória não é enfeite: é requisito de produto (doc 10) e de compliance (doc 09).
 * O advogado assume a responsabilidade pelo prazo — ele precisa poder conferir cada
 * dia contado e cada dia pulado, e entender por quê. Um número sozinho não serve.
 *
 * @param dataIntimacao     quando a intimação foi recebida/publicada
 * @param dataInicioContagem primeiro dia efetivamente contado (CPC art. 224, §§ 2º e 3º)
 * @param dataVencimento    o prazo fatal
 * @param diasCorridosTotais dias de calendário entre a intimação e o vencimento
 * @param passos            um registro por dia analisado — a memória de cálculo
 * @param fundamentacao     as regras aplicadas, em texto
 * @param avisos            ⚠️ o que o motor NÃO garante neste caso
 */
public record ResultadoPrazo(
        LocalDate dataIntimacao,
        LocalDate dataInicioContagem,
        LocalDate dataVencimento,
        int prazoEmDias,
        TipoContagem tipoContagem,
        Justica justica,
        long diasCorridosTotais,
        List<PassoContagem> passos,
        List<String> fundamentacao,
        List<String> avisos
) {

    /**
     * Um dia da contagem.
     *
     * @param data    o dia
     * @param contado se contou como dia de prazo
     * @param motivo  por que contou ou por que não contou
     * @param numero  posição na contagem (0 quando não contado)
     */
    public record PassoContagem(
            LocalDate data,
            boolean contado,
            String motivo,
            int numero
    ) {}
}
