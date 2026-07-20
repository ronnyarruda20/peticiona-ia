package br.com.peticiona.leads;

import org.springframework.stereotype.Service;

@Service
public class InteresseService {

    public void registrar(InteresseController.InteresseRequest request) {
        // Para a fase atual, apenas validamos e registramos a intenção de forma local.
        // Em seguida, esse ponto pode virar persistência, webhook ou integração com CRM.
        if (request == null || request.nome() == null || request.email() == null || request.interesse() == null) {
            throw new IllegalArgumentException("Dados incompletos.");
        }
    }
}
