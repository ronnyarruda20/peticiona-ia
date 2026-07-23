package br.com.peticiona.djen;

import br.com.peticiona.auth.Usuario;
import br.com.peticiona.auth.UsuarioRepository;
import br.com.peticiona.demo.Intimacao;
import br.com.peticiona.demo.IntimacaoRepository;
import br.com.peticiona.demo.Processo;
import br.com.peticiona.demo.ProcessoRepository;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Traz as publicações do advogado para o acervo dele.
 *
 * <p>Converte cada comunicação do DJEN em um {@link Processo} (criado uma vez) e uma
 * {@link Intimacao} (uma por publicação).
 *
 * <p><b>O que este serviço deliberadamente NÃO faz: disparar a IA.</b> As intimações
 * nascem como não lidas e esperam o clique do advogado. Trinta dias de histórico podem ser
 * centenas de publicações; classificar tudo automaticamente gastaria o crédito do modelo
 * de uma vez, estouraria o teto diário do usuário e — pior — encheria a agenda de prazos
 * que ninguém pediu para calcular.
 *
 * <p><b>Sobre o processo sem cliente:</b> a API entrega as partes com seus polos e os
 * advogados com sua OAB, mas não diz qual advogado representa qual parte. Por isso o
 * processo nasce com {@code clientePolo} nulo, e é o advogado quem responde — uma vez por
 * processo, não a cada publicação.
 */
@Service
public class SincronizadorDjen {

    private static final Logger log = LoggerFactory.getLogger(SincronizadorDjen.class);

    /** Janela da primeira carga, quando o advogado acaba de cadastrar a OAB. */
    static final int DIAS_DA_CARGA_INICIAL = 30;

    /**
     * Quantos dias reprocessar antes do último ponto sincronizado.
     *
     * <p>O CNJ pode publicar com atraso, e a aplicação pode ter ficado fora do ar. A
     * sobreposição custa pouco — a deduplicação por hash descarta o que já temos — e evita
     * o único erro que não dá para corrigir depois: um prazo que nunca apareceu.
     */
    static final int DIAS_DE_SOBREPOSICAO = 3;

    private final DjenCliente djen;
    private final UsuarioRepository usuarios;
    private final ProcessoRepository processos;
    private final IntimacaoRepository intimacoes;

    public SincronizadorDjen(DjenCliente djen, UsuarioRepository usuarios,
                             ProcessoRepository processos, IntimacaoRepository intimacoes) {
        this.djen = djen;
        this.usuarios = usuarios;
        this.processos = processos;
        this.intimacoes = intimacoes;
    }

    /**
     * Sincroniza um advogado e devolve quantas publicações novas entraram.
     *
     * <p>Falha da fonte é registrada no usuário e relançada: quem chamou decide se para
     * tudo ou segue para o próximo. O que não pode é a falha sumir.
     */
    @Transactional
    public int sincronizar(Usuario usuario) {
        if (!usuario.temOab()) {
            return 0;
        }

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = usuario.getDjenSincronizadoAte() == null
                ? hoje.minusDays(DIAS_DA_CARGA_INICIAL)
                : usuario.getDjenSincronizadoAte().minusDays(DIAS_DE_SOBREPOSICAO);

        try {
            List<PublicacaoDjen> publicacoes =
                    djen.buscar(usuario.getOabNumero(), usuario.getOabUf(), inicio, hoje);

            int novas = 0;
            for (PublicacaoDjen p : publicacoes) {
                if (importar(usuario, p)) {
                    novas++;
                }
            }

            usuario.registrarSincronizacao(hoje);
            usuarios.save(usuario);

            log.info("OAB {}/{}: {} publicações no intervalo {} a {}, {} novas.",
                    usuario.getOabNumero(), usuario.getOabUf(), publicacoes.size(), inicio, hoje, novas);
            return novas;

        } catch (RuntimeException e) {
            log.error("Falha ao sincronizar a OAB {}/{}.", usuario.getOabNumero(), usuario.getOabUf(), e);
            usuario.registrarFalhaDeSincronizacao(
                    "Não consegui consultar o DJEN na última tentativa. Vou tentar de novo.");
            usuarios.save(usuario);
            throw e;
        }
    }

    /** @return {@code true} se a publicação virou intimação nova */
    private boolean importar(Usuario dono, PublicacaoDjen p) {
        if (!p.aproveitavel()) {
            return false;
        }
        // A trava definitiva é o índice único no banco; esta checagem só evita o trabalho.
        if (intimacoes.existsByUsuarioAndDjenHash(dono, p.hash())) {
            return false;
        }

        Processo processo = processoDe(dono, p);
        intimacoes.save(Intimacao.doDjen(
                dono, processo, p.dataDisponibilizacao(), p.nomeOrgao(), p.texto(),
                p.hash(), p.id(), p.link(), p.tipoComunicacao()));
        return true;
    }

    private Processo processoDe(Usuario dono, PublicacaoDjen p) {
        return processos.findByUsuarioAndNumeroSemMascara(dono, p.numeroProcesso())
                .orElseGet(() -> processos.save(Processo.doDjen(
                        dono,
                        p.numeroParaExibir(),
                        p.numeroProcesso(),
                        p.nomeOrgao(),
                        p.siglaTribunal(),
                        p.nomeClasse(),
                        p.partesDoPolo("A"),
                        p.partesDoPolo("P"))));
    }
}
