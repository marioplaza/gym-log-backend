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
 * @param jwtKey La clave secreta para firmar y verificar los JWT de la aplicación.
 * @param social Configuraciones específicas para proveedores sociales.
 */
@ConfigurationProperties(prefix = "gymlog.security")
public record SecurityProperties(
    List<String> issuerUris, 
    String jwtKey,
    SocialConfig social
) {
    
    /**
     * Configuraciones para proveedores sociales
     */
    public record SocialConfig(
        GoogleConfig google,
        FacebookConfig facebook,
        AppleConfig apple
    ) {}
    
    /**
     * Configuración específica para Google
     */
    public record GoogleConfig(
        List<String> clientIds
    ) {}
    
    /**
     * Configuración específica para Facebook
     */
    public record FacebookConfig(
        String appId,
        String appSecret
    ) {}
    
    /**
     * Configuración específica para Apple
     */
    public record AppleConfig(
        String teamId,
        String bundleId
    ) {}
}