package br.com.peticiona.prazo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Os testes que impedem o pior risco do produto.
 *
 * <p>Doc 09: "Erro no cálculo de prazo — impacto ALTÍSSIMO". Doc 13: o cálculo de prazo
 * é o único componente que exige <b>100% de acerto</b>, porque é determinístico — não há
 * desculpa estatística para errar.
 *
 * <p>Cada caso aqui é verificável na mão, com calendário. Se um destes quebrar, não
 * existe deploy.
 */
class CalculadoraPrazoTest {

    private final CalendarioForense calendario = new CalendarioForense();
    private final CalculadoraPrazo calculadora = new CalculadoraPrazo(calendario);

    private ResultadoPrazo calcular(String intimacao, int dias) {
        return calculadora.calcular(LocalDate.parse(intimacao), dias,
                TipoContagem.DIAS_UTEIS, Justica.TRABALHISTA, true);
    }

    @Nested
    @DisplayName("Regra básica (CPC art. 224)")
    class RegraBasica {

        @Test
        @DisplayName("exclui o dia do começo e conta a partir do dia útil seguinte")
        void excluiDiaDoComeco() {
            // Segunda 07/07/2025. Contagem começa terça 08/07. 5 dias úteis: 08,09,10,11,14.
            ResultadoPrazo r = calcular("2025-07-07", 5);

            assertThat(r.dataInicioContagem()).isEqualTo(LocalDate.parse("2025-07-08"));
            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-07-14"));
        }

        @Test
        @DisplayName("pula o fim de semana no meio da contagem")
        void pulaFimDeSemana() {
            // Quinta 03/07/2025. Conta 04(sex), 07(seg), 08(ter) — 05 e 06 são fim de semana.
            ResultadoPrazo r = calcular("2025-07-03", 3);

            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-07-08"));
            assertThat(r.passos())
                    .filteredOn(p -> !p.contado() && p.motivo().contains("sábado"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("intimação na sexta: contagem só começa na segunda")
        void intimacaoNaSexta() {
            // Sexta 04/07/2025 → sábado e domingo não contam → começa segunda 07/07.
            ResultadoPrazo r = calcular("2025-07-04", 1);

            assertThat(r.dataInicioContagem()).isEqualTo(LocalDate.parse("2025-07-07"));
            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-07-07"));
        }
    }

    @Nested
    @DisplayName("Feriados forenses")
    class Feriados {

        @Test
        @DisplayName("pula feriado nacional fixo")
        void pulaFeriadoFixo() {
            // Intimação seg 08/09/2025; 07/09 (Independência) caiu no domingo anterior.
            // Contagem: 09,10,11,12,15 (13 e 14 fim de semana).
            ResultadoPrazo r = calcular("2025-09-08", 5);
            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-09-15"));
        }

        @Test
        @DisplayName("pula Carnaval (feriado móvel derivado da Páscoa)")
        void pulaCarnaval() {
            // Páscoa 2025: 20/04. Carnaval: 03 e 04/03. Cinzas: 05/03.
            // Intimação sex 28/02/2025 → contagem começa 06/03 (quinta), pulando 03,04,05.
            ResultadoPrazo r = calcular("2025-02-28", 3);

            assertThat(r.dataInicioContagem()).isEqualTo(LocalDate.parse("2025-03-06"));
            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-03-10"));
            assertThat(r.passos())
                    .filteredOn(p -> p.motivo().contains("Carnaval"))
                    .hasSize(2);
        }

        @Test
        @DisplayName("Páscoa é calculada corretamente para vários anos")
        void pascoaCorreta() {
            assertThat(CalendarioForense.calcularPascoa(2024)).isEqualTo(LocalDate.parse("2024-03-31"));
            assertThat(CalendarioForense.calcularPascoa(2025)).isEqualTo(LocalDate.parse("2025-04-20"));
            assertThat(CalendarioForense.calcularPascoa(2026)).isEqualTo(LocalDate.parse("2026-04-05"));
            assertThat(CalendarioForense.calcularPascoa(2027)).isEqualTo(LocalDate.parse("2027-03-28"));
        }

        @Test
        @DisplayName("Consciência Negra só é feriado nacional a partir de 2024")
        void conscienciaNegra() {
            assertThat(calendario.isDiaUtil(LocalDate.parse("2023-11-20"), Justica.ESTADUAL)).isTrue();
            assertThat(calendario.isDiaUtil(LocalDate.parse("2024-11-20"), Justica.ESTADUAL)).isFalse();
        }

        @Test
        @DisplayName("Justiça Federal tem feriados próprios (Lei 5.010/66)")
        void feriadosJusticaFederal() {
            LocalDate diaDoAdvogado = LocalDate.parse("2025-08-11"); // segunda-feira
            assertThat(calendario.isDiaUtil(diaDoAdvogado, Justica.FEDERAL)).isFalse();
            assertThat(calendario.isDiaUtil(diaDoAdvogado, Justica.TRABALHISTA)).isTrue();
        }
    }

    @Nested
    @DisplayName("Recesso forense (CPC art. 220) — o caso que mais dá erro")
    class Recesso {

        @Test
        @DisplayName("prazo que atravessa o recesso só volta a correr em 21/01")
        void atravessaRecesso() {
            // Intimação 15/12/2025 (seg). Conta 16,17,18,19 (4 dias úteis).
            // 20/12 a 20/01 suspenso. Retoma 21/01/2026 (quarta) com o 5º dia.
            ResultadoPrazo r = calcular("2025-12-15", 5);

            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2026-01-21"));
            assertThat(r.passos())
                    .filteredOn(p -> p.motivo().contains("recesso"))
                    .isNotEmpty();
        }

        @Test
        @DisplayName("intimação dentro do recesso: contagem só começa depois de 20/01")
        void intimacaoNoRecesso() {
            ResultadoPrazo r = calcular("2025-12-26", 15);

            assertThat(r.dataInicioContagem()).isEqualTo(LocalDate.parse("2026-01-21"));
            assertThat(r.dataInicioContagem().getYear()).isEqualTo(2026);
        }

        @Test
        @DisplayName("desligar o recesso muda o resultado — e o motor avisa")
        void recessoDesligado() {
            ResultadoPrazo comRecesso = calcular("2025-12-15", 5);
            ResultadoPrazo semRecesso = calculadora.calcular(
                    LocalDate.parse("2025-12-15"), 5,
                    TipoContagem.DIAS_UTEIS, Justica.TRABALHISTA, false);

            assertThat(semRecesso.dataVencimento()).isBefore(comRecesso.dataVencimento());
            assertThat(semRecesso.avisos())
                    .anyMatch(a -> a.contains("atravessa o período de 20/12"));
        }
    }

    @Nested
    @DisplayName("Prorrogação do vencimento (art. 224, § 1º)")
    class Prorrogacao {

        @Test
        @DisplayName("dias corridos que vencem no sábado prorrogam para segunda")
        void prorrogaVencimento() {
            // 04/07/2025 (sex) + 7 dias corridos = 11/07 (sex). Ajusto para cair no fim de semana:
            // 06/07/2025 (dom) + 6 dias corridos = 12/07 (sáb) → prorroga para 14/07 (seg).
            ResultadoPrazo r = calculadora.calcular(
                    LocalDate.parse("2025-07-06"), 6,
                    TipoContagem.DIAS_CORRIDOS, Justica.ESTADUAL, false);

            assertThat(r.dataVencimento()).isEqualTo(LocalDate.parse("2025-07-14"));
            assertThat(r.passos())
                    .anyMatch(p -> p.motivo().contains("prorrogado"));
        }
    }

    @Nested
    @DisplayName("Memória de cálculo e avisos")
    class Memoria {

        @Test
        @DisplayName("registra exatamente N dias contados, numerados em ordem")
        void memoriaCompleta() {
            ResultadoPrazo r = calcular("2025-07-07", 15);

            assertThat(r.passos()).filteredOn(ResultadoPrazo.PassoContagem::contado).hasSize(15);
            assertThat(r.passos()).filteredOn(ResultadoPrazo.PassoContagem::contado)
                    .extracting(ResultadoPrazo.PassoContagem::numero)
                    .containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
        }

        @Test
        @DisplayName("todo resultado carrega a fundamentação e os avisos de limite")
        void fundamentacaoEAvisos() {
            ResultadoPrazo r = calcular("2025-07-07", 15);

            assertThat(r.fundamentacao()).anyMatch(f -> f.contains("art. 224"));
            assertThat(r.fundamentacao()).anyMatch(f -> f.contains("art. 219"));
            assertThat(r.avisos()).anyMatch(a -> a.contains("Você é o responsável"));
            assertThat(r.avisos()).anyMatch(a -> a.contains("estaduais e municipais"));
        }

        @Test
        @DisplayName("o último dia contado é sempre o vencimento")
        void ultimoDiaContadoEhVencimento() {
            ResultadoPrazo r = calcular("2025-03-10", 30);

            LocalDate ultimo = r.passos().stream()
                    .filter(ResultadoPrazo.PassoContagem::contado)
                    .reduce((a, b) -> b)
                    .orElseThrow()
                    .data();

            assertThat(ultimo).isEqualTo(r.dataVencimento());
        }
    }

    @Nested
    @DisplayName("Entradas inválidas")
    class Validacao {

        @Test
        void recusaPrazoZeroOuNegativo() {
            assertThatThrownBy(() -> calcular("2025-07-07", 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pelo menos 1 dia");
        }

        @Test
        void recusaDataNula() {
            assertThatThrownBy(() -> calculadora.calcular(
                    null, 15, TipoContagem.DIAS_UTEIS, Justica.TRABALHISTA, true))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void recusaPrazoAbsurdo() {
            assertThatThrownBy(() -> calcular("2025-07-07", 99999))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("limite");
        }
    }

    @Nested
    @DisplayName("Determinismo (a propriedade que justifica não usar LLM)")
    class Determinismo {

        @Test
        @DisplayName("mil execuções, sempre o mesmo resultado")
        void mesmoInputMesmoOutput() {
            LocalDate esperado = calcular("2025-12-15", 15).dataVencimento();
            for (int i = 0; i < 1000; i++) {
                assertThat(calcular("2025-12-15", 15).dataVencimento()).isEqualTo(esperado);
            }
        }
    }
}
