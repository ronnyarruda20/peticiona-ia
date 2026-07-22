package br.com.peticiona.demo;

import br.com.peticiona.auth.Usuario;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Mesma regra do {@link IntimacaoRepository}: nada se busca sem dizer de quem é. */
public interface ProcessoRepository extends JpaRepository<Processo, UUID> {

    List<Processo> findByUsuario(Usuario usuario);

    void deleteByUsuario(Usuario usuario);
}
