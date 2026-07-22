package br.com.peticiona.leads;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InteresseRepository extends JpaRepository<Interesse, UUID> {
}
