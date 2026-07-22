package br.com.peticiona.leads;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InteresseService {

    private final InteresseRepository interesses;

    public InteresseService(InteresseRepository interesses) {
        this.interesses = interesses;
    }

    /** Antes o lead era validado e descartado; agora ele tem para onde ir. */
    @Transactional
    public void registrar(InteresseController.InteresseRequest request) {
        if (request == null || request.nome() == null || request.email() == null || request.interesse() == null) {
            throw new IllegalArgumentException("Dados incompletos.");
        }
        interesses.save(new Interesse(
                request.nome(), request.email(), request.interesse(), request.mensagem()));
    }
}
