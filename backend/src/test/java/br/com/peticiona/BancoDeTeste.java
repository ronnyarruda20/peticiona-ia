package br.com.peticiona;

import java.sql.DriverManager;
import org.springframework.test.context.DynamicPropertyRegistry;

/**
 * Aponta os testes para um Postgres real, subido fora do Maven.
 *
 * <p><b>Por que não Testcontainers:</b> o cliente Docker que ele embute não conversa com o
 * daemon 29.x no Windows — a negociação de versão da API falha antes de qualquer container
 * subir. Em vez de brigar com isso, os testes usam um banco que se sobe uma vez:
 *
 * <pre>
 * docker run -d --name peticiona-test-db \
 *   -e POSTGRES_USER=peticiona -e POSTGRES_PASSWORD=peticiona -e POSTGRES_DB=peticiona_test \
 *   -p 55432:5432 postgres:16-alpine
 * </pre>
 *
 * <p><b>Por que Postgres de verdade e não H2:</b> o isolamento entre advogados depende de
 * chave estrangeira, de {@code ON DELETE CASCADE} e das migrações do Flyway. Um banco em
 * memória fingindo ser Postgres provaria bem menos do que aparenta.
 *
 * <p>Sem o banco no ar os testes que dependem dele são <b>pulados</b>, não quebrados: o
 * build da imagem no Railway roda {@code mvn package} onde não há Docker, e falhar ali
 * impediria de publicar sem proteger ninguém.
 */
public final class BancoDeTeste {

    private static final String URL = System.getenv().getOrDefault(
            "PETICIONA_TEST_DB_URL", "jdbc:postgresql://localhost:55432/peticiona_test");
    private static final String USUARIO = "peticiona";
    private static final String SENHA = "peticiona";

    private BancoDeTeste() {
    }

    /** Usado por {@code @EnabledIf} para pular a classe inteira quando não há banco. */
    public static boolean disponivel() {
        try (var conexao = DriverManager.getConnection(URL, USUARIO, SENHA)) {
            return conexao.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }

    public static void configurar(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", () -> URL);
        registro.add("spring.datasource.username", () -> USUARIO);
        registro.add("spring.datasource.password", () -> SENHA);
        // Cada execução parte de um banco limpo: teste que depende de sobra da rodada
        // anterior não prova nada.
        registro.add("spring.flyway.clean-disabled", () -> "false");
        registro.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }
}
