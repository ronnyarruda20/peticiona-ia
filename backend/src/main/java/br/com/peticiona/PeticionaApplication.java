package br.com.peticiona;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @see br.com.peticiona.djen.RotinaDjen para a busca diária de publicações — é ela que
 *      exige {@code @EnableScheduling}, e a carga inicial da OAB que exige {@code @EnableAsync}.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
public class PeticionaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PeticionaApplication.class, args);
    }
}
