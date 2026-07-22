package br.com.peticiona.ia;

import br.com.peticiona.demo.IntimacaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grava o desfecho do disparo do fluxo de IA.
 *
 * <p>Existe como bean separado por um motivo concreto: o resultado do disparo chega numa
 * thread do cliente HTTP, muito depois de a requisição do usuário ter terminado. Ali não
 * há transação nem entidade gerenciada — a intimação precisa ser recarregada e gravada de
 * novo.
 *
 * <p>E precisa ser <b>outra classe</b>, não um método do {@link FluxoIa}: chamada de um
 * método do próprio bean não passa pelo proxy do Spring, e a anotação
 * {@code @Transactional} seria silenciosamente ignorada.
 */
@Service
public class EstadoDoFluxo {

    private final IntimacaoRepository intimacoes;

    public EstadoDoFluxo(IntimacaoRepository intimacoes) {
        this.intimacoes = intimacoes;
    }

    @Transactional
    public void registrarFalha(UUID intimacaoId, String motivo) {
        intimacoes.findById(intimacaoId).ifPresent(i -> i.marcarFalha(motivo));
    }
}
