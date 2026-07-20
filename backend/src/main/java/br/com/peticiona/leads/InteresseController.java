package br.com.peticiona.leads;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interesses")
@CrossOrigin(origins = "*")
public class InteresseController {

    private final InteresseService interesseService;

    public InteresseController(InteresseService interesseService) {
        this.interesseService = interesseService;
    }

    @PostMapping
    public ResponseEntity<InteresseResponse> registrar(@Valid @RequestBody InteresseRequest request) {
        interesseService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new InteresseResponse(true, "Obrigado por se interessar!"));
    }

    public record InteresseRequest(
            @NotBlank(message = "Informe o seu nome.") String nome,
            @NotBlank(message = "Informe um e-mail.") @Email(message = "E-mail inválido.") String email,
            @NotBlank(message = "Informe o tipo de interesse.") String interesse,
            String mensagem
    ) {}

    public record InteresseResponse(boolean sucesso, String mensagem) {}
}
