package br.com.peticiona.prazo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Calcula prazos processuais de forma determinística.
 *
 * <p><b>Regra número um do projeto: LLM nunca calcula data.</b> (doc 06). Este serviço é
 * aritmética de calendário — dado o mesmo input, sempre o mesmo output, e cada passo
 * fica registrado na memória de cálculo.
 *
 * <p>Regras implementadas (CPC/2015):
 * <ul>
 *   <li><b>Art. 224:</b> exclui-se o dia do começo, inclui-se o do vencimento.</li>
 *   <li><b>Art. 224, § 1º:</b> vencimento em dia não útil prorroga para o próximo útil.</li>
 *   <li><b>Art. 224, § 3º:</b> a contagem começa no primeiro dia útil seguinte à intimação.</li>
 *   <li><b>Art. 219:</b> prazos processuais em dias úteis (idem CLT art. 775).</li>
 *   <li><b>Art. 220:</b> suspensão do curso do prazo entre 20/12 e 20/01.</li>
 * </ul>
 *
 * <p>⚠️ <b>Não substitui conferência humana.</b> Feriados estaduais/municipais,
 * portarias de suspensão de expediente e prazos com regra própria (dobro para a Fazenda
 * Pública, Defensoria, litisconsortes com procuradores distintos) NÃO estão cobertos.
 */
@Service
public class CalculadoraPrazo {

    /** Trava de segurança: nenhum prazo processual comum chega perto disso. */
    private static final int LIMITE_DIAS = 3650;

    private final CalendarioForense calendario;

    public CalculadoraPrazo(CalendarioForense calendario) {
        this.calendario = calendario;
    }

    public ResultadoPrazo calcular(
            LocalDate dataIntimacao,
            int prazoEmDias,
            TipoContagem tipoContagem,
            Justica justica,
            boolean considerarRecesso) {

        if (dataIntimacao == null) {
            throw new IllegalArgumentException("Data da intimação é obrigatória.");
        }
        if (prazoEmDias < 1) {
            throw new IllegalArgumentException("O prazo precisa ser de pelo menos 1 dia.");
        }
        if (prazoEmDias > LIMITE_DIAS) {
            throw new IllegalArgumentException("Prazo acima do limite suportado (" + LIMITE_DIAS + " dias).");
        }

        List<ResultadoPrazo.PassoContagem> passos = new ArrayList<>();
        List<String> avisos = new ArrayList<>();

        // ── CPC art. 224, § 3º: a contagem começa no primeiro dia ÚTIL seguinte ──
        LocalDate cursor = dataIntimacao.plusDays(1);
        while (!ehDiaContavel(cursor, justica, considerarRecesso)) {
            passos.add(new ResultadoPrazo.PassoContagem(
                    cursor, false, motivoNaoUtil(cursor, justica, considerarRecesso) + " — contagem ainda não iniciou", 0));
            cursor = cursor.plusDays(1);
        }
        LocalDate inicioContagem = cursor;

        // ── Conta os dias ──
        int contados = 0;
        LocalDate vencimento = null;

        while (contados < prazoEmDias) {
            boolean conta;
            String motivo;

            if (tipoContagem == TipoContagem.DIAS_UTEIS) {
                conta = ehDiaContavel(cursor, justica, considerarRecesso);
                motivo = conta ? "dia útil" : motivoNaoUtil(cursor, justica, considerarRecesso);
            } else {
                // Dias corridos: conta tudo, mas o recesso ainda suspende o curso (art. 220).
                boolean suspenso = considerarRecesso && calendario.isRecessoForense(cursor);
                conta = !suspenso;
                motivo = conta ? "dia corrido" : "recesso forense (CPC art. 220)";
            }

            if (conta) {
                contados++;
                passos.add(new ResultadoPrazo.PassoContagem(cursor, true, motivo, contados));
                if (contados == prazoEmDias) {
                    vencimento = cursor;
                }
            } else {
                passos.add(new ResultadoPrazo.PassoContagem(cursor, false, motivo, 0));
            }
            cursor = cursor.plusDays(1);
        }

        // ── CPC art. 224, § 1º: vencimento em dia não útil prorroga ──
        while (!ehDiaContavel(vencimento, justica, considerarRecesso)) {
            passos.add(new ResultadoPrazo.PassoContagem(
                    vencimento, false,
                    motivoNaoUtil(vencimento, justica, considerarRecesso) + " — vencimento prorrogado (CPC art. 224, § 1º)", 0));
            vencimento = vencimento.plusDays(1);
        }

        montarAvisos(avisos, dataIntimacao, vencimento, considerarRecesso);

        return new ResultadoPrazo(
                dataIntimacao,
                inicioContagem,
                vencimento,
                prazoEmDias,
                tipoContagem,
                justica,
                ChronoUnit.DAYS.between(dataIntimacao, vencimento),
                passos,
                montarFundamentacao(tipoContagem, considerarRecesso),
                avisos
        );
    }

    private boolean ehDiaContavel(LocalDate data, Justica justica, boolean considerarRecesso) {
        if (considerarRecesso && calendario.isRecessoForense(data)) {
            return false;
        }
        return calendario.isDiaUtil(data, justica);
    }

    private String motivoNaoUtil(LocalDate data, Justica justica, boolean considerarRecesso) {
        if (considerarRecesso && calendario.isRecessoForense(data)) {
            return "recesso forense (CPC art. 220)";
        }
        String feriado = calendario.nomeDoFeriado(data, justica);
        if (feriado != null) {
            return "feriado: " + feriado;
        }
        return switch (data.getDayOfWeek()) {
            case SATURDAY -> "sábado";
            case SUNDAY -> "domingo";
            default -> "dia não útil";
        };
    }

    private List<String> montarFundamentacao(TipoContagem tipo, boolean considerarRecesso) {
        List<String> f = new ArrayList<>();
        f.add("CPC art. 224: exclui-se o dia do começo e inclui-se o do vencimento.");
        f.add("CPC art. 224, § 3º: a contagem inicia no primeiro dia útil seguinte à intimação.");
        f.add("CPC art. 224, § 1º: vencimento em dia sem expediente prorroga para o dia útil seguinte.");
        if (tipo == TipoContagem.DIAS_UTEIS) {
            f.add("CPC art. 219 (e CLT art. 775): prazos processuais contam-se em dias úteis.");
        } else {
            f.add("Contagem em dias corridos — típica de prazo de direito material (prescrição/decadência).");
        }
        if (considerarRecesso) {
            f.add("CPC art. 220: suspensão do curso do prazo entre 20/12 e 20/01.");
        }
        return f;
    }

    private void montarAvisos(List<String> avisos, LocalDate intimacao, LocalDate vencimento, boolean recesso) {
        avisos.add("Confira sempre: este cálculo não substitui sua conferência. Você é o responsável pelo prazo.");
        avisos.add("Feriados estaduais e municipais NÃO estão considerados — um feriado na comarca desloca o vencimento.");
        avisos.add("Portarias de suspensão de expediente (greve, luto oficial, instabilidade do sistema) não estão cobertas.");
        avisos.add("Prazos em dobro (Fazenda Pública, Defensoria, MP, litisconsortes com procuradores distintos) não são aplicados automaticamente.");

        if (!recesso && cruzaRecesso(intimacao, vencimento)) {
            avisos.add("⚠️ Este prazo atravessa o período de 20/12 a 20/01 e você desligou a suspensão do recesso. Confirme se é isso mesmo.");
        }
        if (intimacao.getYear() != vencimento.getYear()) {
            avisos.add("Este prazo cruza a virada do ano — confira os feriados móveis do ano seguinte.");
        }
    }

    private boolean cruzaRecesso(LocalDate inicio, LocalDate fim) {
        LocalDate d = inicio;
        while (!d.isAfter(fim)) {
            if (calendario.isRecessoForense(d)) {
                return true;
            }
            d = d.plusDays(1);
        }
        return false;
    }
}
