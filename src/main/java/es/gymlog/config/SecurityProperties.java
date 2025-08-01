package es.gymlog.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

/**
 * Propiedades de configuración de seguridad personalizadas para la aplicación.
 * <p>
 * Se utiliza para vincular de forma segura las propiedades definidas bajo el prefijo "gymlog.security"
 * en el archivo {@code application.yml}. Este enfoque es preferible a @Value por ser más robusto y type-safe.
 *
 * @param issuerUris Lista de URIs de los emisores de JWT en los que se confía.
 */
@ConfigurationProperties(prefix = "gymlog.security")
public record SecurityProperties(List<String> issuerUris) {
}
