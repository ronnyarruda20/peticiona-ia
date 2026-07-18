package br.com.peticiona.prazo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API da calculadora de prazos.
 *
 * <p>É pública de propósito: além de ser o núcleo do produto, a calculadora é o ímã de
 * leads da estratégia de conteúdo (doc 07) — fica aberta no site, sem cadastro.
 */
@RestController
@RequestMapping("/api/prazos")
@CrossOrigin(origins = "*")
public class PrazoController {

    private final CalculadoraPrazo calculadora;

    public PrazoController(CalculadoraPrazo calculadora) {
        this.calculadora = calculadora;
    }

    @PostMapping("/calcular")
    public ResultadoPrazo calcular(@Valid @RequestBody CalculoRequest req) {
        return calculadora.calcular(
                req.dataIntimacao(),
                req.prazoEmDias(),
                req.tipoContagem() == null ? TipoContagem.DIAS_UTEIS : req.tipoContagem(),
                req.justica() == null ? Justica.TRABALHISTA : req.justica(),
                req.considerarRecesso() == null || req.considerarRecesso()
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> erroDeEntrada(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
    }

    public record CalculoRequest(
            @NotNull(message = "Informe a data da intimação.")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dataIntimacao,

            @Min(value = 1, message = "O prazo precisa ser de pelo menos 1 dia.")
            @Max(value = 3650, message = "Prazo acima do limite suportado.")
            int prazoEmDias,

            TipoContagem tipoContagem,
            Justica justica,
            Boolean considerarRecesso
    ) {}
}
