package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acesso às intimações, sempre no escopo de um usuário.
 *
 * <p><b>Repare que não existe um {@code findById} simples aqui.</b> É de propósito: um
 * método que busca por id sem o dono é um vazamento esperando para acontecer — basta
 * alguém chamar com o id de outro advogado. Ao exigir o {@link Usuario} na assinatura, o
 * isolamento vira responsabilidade do compilador, não da memória de quem escreve o
 * controller. O {@code findById} herdado do {@link JpaRepository} não deve ser usado.
 */
public interface IntimacaoRepository extends JpaRepository<Intimacao, UUID> {

    Optional<Intimacao> findByIdAndUsuario(UUID id, Usuario usuario);

    List<Intimacao> findByUsuarioOrderByCriadoEmAsc(Usuario usuario);

    void deleteByUsuario(Usuario usuario);

    /** Remove só as intimações de processos de uma origem — preserva as reais do DJEN. */
    void deleteByUsuarioAndProcessoOrigem(Usuario usuario, String origem);

    boolean existsByUsuario(Usuario usuario);

    /** Já importamos esta publicação para este advogado? */
    boolean existsByUsuarioAndDjenHash(Usuario usuario, String djenHash);
}
