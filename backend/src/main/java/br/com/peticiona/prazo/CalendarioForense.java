package br.com.peticiona.prazo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Sabe quais dias são úteis para fins processuais.
 *
 * <p><b>Nenhuma data aqui vem de LLM.</b> É tabela curada e algoritmo determinístico —
 * exigência do doc 06 e mitigação do risco ALTÍSSIMO do doc 09 (erro de prazo).
 *
 * <p><b>⚠️ Verificação jurídica pendente.</b> As regras abaixo foram implementadas a
 * partir da legislação citada, mas <b>precisam ser conferidas por advogado</b> antes de
 * qualquer uso real. Especificamente:
 * <ul>
 *   <li>Carnaval e Corpus Christi são pontos facultativos na esfera federal, mas
 *       tradicionalmente suspendem expediente forense — a prática varia por tribunal.</li>
 *   <li>Feriados estaduais e municipais <b>não estão cobertos</b> (ver {@link Justica}).</li>
 *   <li>Portarias de suspensão de expediente (greve, mudança de sistema, luto oficial)
 *       não estão cobertas e são fonte real de erro.</li>
 * </ul>
 */
@Component
public class CalendarioForense {

    /** Consciência Negra virou feriado nacional pela Lei 14.759/2023. */
    private static final int ANO_CONSCIENCIA_NEGRA_NACIONAL = 2024;

    /**
     * Retorna todos os feriados forenses do ano, com o nome de cada um.
     * O nome importa: ele aparece na memória de cálculo que o advogado confere.
     */
    public Map<LocalDate, String> feriadosDoAno(int ano, Justica justica) {
        Map<LocalDate, String> feriados = new LinkedHashMap<>();

        // ── Feriados nacionais fixos (Lei 662/1949 e Lei 10.607/2002) ──
        feriados.put(LocalDate.of(ano, 1, 1), "Confraternização Universal");
        feriados.put(LocalDate.of(ano, 4, 21), "Tiradentes");
        feriados.put(LocalDate.of(ano, 5, 1), "Dia do Trabalho");
        feriados.put(LocalDate.of(ano, 9, 7), "Independência");
        feriados.put(LocalDate.of(ano, 10, 12), "Nossa Senhora Aparecida");
        feriados.put(LocalDate.of(ano, 11, 2), "Finados");
        feriados.put(LocalDate.of(ano, 11, 15), "Proclamação da República");
        feriados.put(LocalDate.of(ano, 12, 25), "Natal");

        if (ano >= ANO_CONSCIENCIA_NEGRA_NACIONAL) {
            feriados.put(LocalDate.of(ano, 11, 20), "Consciência Negra");
        }

        // ── Feriados móveis, derivados da Páscoa ──
        LocalDate pascoa = calcularPascoa(ano);
        feriados.put(pascoa.minusDays(48), "Carnaval (segunda-feira)");
        feriados.put(pascoa.minusDays(47), "Carnaval (terça-feira)");
        feriados.put(pascoa.minusDays(46), "Quarta-feira de Cinzas");
        feriados.put(pascoa.minusDays(2), "Sexta-feira Santa");
        feriados.put(pascoa.plusDays(60), "Corpus Christi");

        // ── Feriados próprios da Justiça (Lei 5.010/1966, art. 62) ──
        // ⚠️ Aplicados à Justiça Federal; a prática em outros ramos varia por tribunal.
        if (justica == Justica.FEDERAL) {
            feriados.put(LocalDate.of(ano, 8, 11), "Dia do Advogado (JF — Lei 5.010/66)");
            feriados.put(LocalDate.of(ano, 11, 1), "Dia de Todos os Santos (JF — Lei 5.010/66)");
            feriados.put(LocalDate.of(ano, 12, 8), "Nossa Senhora da Conceição (JF — Lei 5.010/66)");
        }

        return feriados;
    }

    /** É dia útil para fins processuais? (não é sábado, domingo nem feriado forense) */
    public boolean isDiaUtil(LocalDate data, Justica justica) {
        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.SATURDAY || dia == DayOfWeek.SUNDAY) {
            return false;
        }
        return !feriadosDoAno(data.getYear(), justica).containsKey(data);
    }

    /** Nome do feriado, ou null se não for feriado. Usado na memória de cálculo. */
    public String nomeDoFeriado(LocalDate data, Justica justica) {
        return feriadosDoAno(data.getYear(), justica).get(data);
    }

    /**
     * O recesso forense suspende o curso dos prazos entre 20/12 e 20/01 (CPC art. 220).
     *
     * <p>⚠️ O art. 220 fala em suspensão do <i>curso do prazo</i>. A Justiça do Trabalho
     * tem regra própria (CLT art. 775-A, incluído pela Lei 13.545/2017), com período de
     * 20/12 a 20/01 também. <b>Confirmar com advogado.</b>
     */
    public boolean isRecessoForense(LocalDate data) {
        int mes = data.getMonthValue();
        int dia = data.getDayOfMonth();
        return (mes == 12 && dia >= 20) || (mes == 1 && dia <= 20);
    }

    /**
     * Domingo de Páscoa pelo algoritmo de Meeus/Jones/Butcher (calendário gregoriano).
     * É aritmética pura e verificável — nada de tabela mágica.
     */
    static LocalDate calcularPascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
