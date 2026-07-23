package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mesma regra do {@link IntimacaoRepository}: nada se busca sem dizer de quem é. */
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {

    List<Processo> findByUsuario(Usuario usuario);

    Optional<Processo> findByIdAndUsuario(UUID id, Usuario usuario);

    /** Casa a publicação com um processo já conhecido — o número cru é a chave. */
    Optional<Processo> findByUsuarioAndNumeroSemMascara(Usuario usuario, String numeroSemMascara);

    /** Processos esperando o advogado dizer de que lado está. */
    List<Processo> findByUsuarioAndClientePoloIsNullAndOrigem(Usuario usuario, String origem);

    /** Processos de uma origem (DEMO ou DJEN) — usado para carregar/remover exemplos. */
    List<Processo> findByUsuarioAndOrigem(Usuario usuario, String origem);

    void deleteByUsuario(Usuario usuario);

    void deleteByUsuarioAndOrigem(Usuario usuario, String origem);
}
