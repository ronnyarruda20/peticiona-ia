package br.com.peticiona.djen;

import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Busca as publicações do dia, para todos os advogados cadastrados.
 *
 * <p>Roda às 8h de Brasília: o DJEN disponibiliza as publicações de madrugada, e o
 * advogado precisa encontrá-las quando abrir o sistema pela manhã — não à tarde.
 *
 * <p><b>Sequencial e com pausa, de propósito.</b> A API do CNJ é pública, gratuita e sem
 * SLA publicado. Disparar consultas em paralelo para acelerar nossa rotina seria usar mal
 * uma infraestrutura que não é nossa e da qual dependemos — e o custo de esperar alguns
 * segundos a mais numa rotina noturna é zero.
 *
 * <p>Falha de um advogado não interrompe os outros: cada erro é registrado nele e a varredura
 * continua. Um tribunal fora do ar não pode deixar todo mundo sem agenda.
 */
@Component
public class RotinaDjen {

    private static final Logger log = LoggerFactory.getLogger(RotinaDjen.class);

    /** Pausa entre advogados. Educação com a fonte, não otimização nossa. */
    private static final long PAUSA_ENTRE_USUARIOS_MS = 1_000;

    private final UsuarioRepository usuarios;
    private final SincronizadorDjen sincronizador;

    public RotinaDjen(UsuarioRepository usuarios, SincronizadorDjen sincronizador) {
        this.usuarios = usuarios;
        this.sincronizador = sincronizador;
    }

    @Scheduled(cron = "${peticiona.djen.cron:0 0 8 * * *}", zone = "America/Sao_Paulo")
    public void varrerTodos() {
        List<Usuario> comOab = usuarios.findAll().stream().filter(Usuario::temOab).toList();
        if (comOab.isEmpty()) {
            return;
        }

        log.info("Sincronização diária do DJEN: {} advogado(s) com OAB cadastrada.", comOab.size());
        int total = 0;

        for (Usuario u : comOab) {
            try {
                total += sincronizador.sincronizar(u);
                Thread.sleep(PAUSA_ENTRE_USUARIOS_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Sincronização diária interrompida.");
                return;
            } catch (RuntimeException e) {
                // Já registrado no usuário pelo sincronizador; aqui só não pode parar a fila.
                log.warn("Pulei a OAB {}/{} nesta rodada.", u.getOabNumero(), u.getOabUf());
            }
        }
        log.info("Sincronização diária concluída: {} publicações novas.", total);
    }

    /**
     * Primeira carga, logo depois de o advogado cadastrar a OAB.
     *
     * <p>Assíncrona porque trinta dias de publicações levam segundos ou minutos, e a tela
     * de cadastro não pode ficar travada esperando. O advogado vê "buscando" e o acervo
     * aparece quando termina.
     */
    @Async
    public void cargaInicial(Usuario usuario) {
        try {
            int novas = sincronizador.sincronizar(usuario);
            log.info("Carga inicial da OAB {}/{}: {} publicações.",
                    usuario.getOabNumero(), usuario.getOabUf(), novas);
        } catch (RuntimeException e) {
            log.error("Carga inicial falhou para a OAB {}/{}.",
                    usuario.getOabNumero(), usuario.getOabUf(), e);
        }
    }
}
