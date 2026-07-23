package br.com.peticiona.djen;

import java.time.LocalDate;
import java.util.List;

/**
 * De onde vêm as publicações.
 *
 * <p>Existe como interface para que o mapeamento de publicação em processo e intimação
 * possa ser testado sem rede: a API do CNJ é pública, gratuita e sem SLA — amarrar a
 * suíte de testes à disponibilidade dela seria trocar um teste por um palpite.
 */
public interface DjenCliente {

    /**
     * Publicações de uma OAB num intervalo de datas.
     *
     * @throws DjenIndisponivelException quando a API do CNJ não responde ou responde erro
     */
    List<PublicacaoDjen> buscar(String numeroOab, String ufOab, LocalDate inicio, LocalDate fim);

    /** A fonte não respondeu. A sincronização falha visivelmente em vez de fingir sucesso. */
    class DjenIndisponivelException extends RuntimeException {
        public DjenIndisponivelException(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }

        public DjenIndisponivelException(String mensagem) {
            super(mensagem);
        }
    }
}
