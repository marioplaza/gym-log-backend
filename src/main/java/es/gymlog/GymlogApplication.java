package es.gymlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal que inicia la aplicación Spring Boot.
 */
@SpringBootApplication
public class GymlogApplication {

    /**
     * Punto de entrada de la aplicación.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        SpringApplication.run(GymlogApplication.class, args);
    }

}
