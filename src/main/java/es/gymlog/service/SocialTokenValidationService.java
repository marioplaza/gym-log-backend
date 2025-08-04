package es.gymlog.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import es.gymlog.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Servicio para validar tokens de proveedores sociales (Google, Apple, Facebook)
 */
@Service
public class SocialTokenValidationService {

    private static final Logger logger = LoggerFactory.getLogger(SocialTokenValidationService.class);
    
    private final WebClient webClient;
    private final GoogleIdTokenVerifier googleVerifier;
    private final SecurityProperties securityProperties;

    public SocialTokenValidationService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        this.webClient = WebClient.builder().build();
        
        // Configurar verificador de Google con los client IDs de la configuración
        if (securityProperties.social() != null && 
            securityProperties.social().google() != null && 
            securityProperties.social().google().clientIds() != null) {
            
            this.googleVerifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), 
                    new GsonFactory())
                    .setAudience(securityProperties.social().google().clientIds())
                    .build();
        } else {
            logger.warn("No se han configurado client IDs para Google. La validación de tokens de Google fallará.");
            this.googleVerifier = null;
        }
    }

    /**
     * Valida un token social y extrae la información del usuario
     */
    public Mono<SocialUserInfo> validateToken(String provider, String token) {
        return switch (provider.toLowerCase()) {
            case "google" -> validateGoogleToken(token);
            case "facebook" -> validateFacebookToken(token);
            case "apple" -> validateAppleToken(token);
            default -> Mono.error(new IllegalArgumentException("Proveedor no soportado: " + provider));
        };
    }

    /**
     * Valida token de Google usando Google API Client
     */
    private Mono<SocialUserInfo> validateGoogleToken(String token) {
        if (googleVerifier == null) {
            return Mono.error(new RuntimeException("Google no está configurado correctamente"));
        }
        
        return Mono.fromCallable(() -> {
            try {
                GoogleIdToken idToken = googleVerifier.verify(token);
                if (idToken != null) {
                    GoogleIdToken.Payload payload = idToken.getPayload();
                    
                    logger.info("Token de Google validado exitosamente para usuario: {}", payload.getEmail());
                    
                    return new SocialUserInfo(
                        payload.getSubject(), // User ID
                        payload.getEmail(),
                        (String) payload.get("name"),
                        (String) payload.get("picture")
                    );
                } else {
                    throw new RuntimeException("Token de Google inválido o expirado");
                }
            } catch (GeneralSecurityException | IOException e) {
                logger.error("Error validando token de Google", e);
                throw new RuntimeException("Error validando token de Google: " + e.getMessage());
            }
        });
    }

    /**
     * Valida token de Facebook usando Facebook Graph API
     */
    private Mono<SocialUserInfo> validateFacebookToken(String token) {
        String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + token;
        
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> {
                if (response.containsKey("error")) {
                    Map<String, Object> error = (Map<String, Object>) response.get("error");
                    String errorMessage = (String) error.get("message");
                    throw new RuntimeException("Token de Facebook inválido: " + errorMessage);
                }
                
                String pictureUrl = null;
                if (response.containsKey("picture")) {
                    Map<String, Object> picture = (Map<String, Object>) response.get("picture");
                    if (picture.containsKey("data")) {
                        Map<String, Object> data = (Map<String, Object>) picture.get("data");
                        pictureUrl = (String) data.get("url");
                    }
                }
                
                logger.info("Token de Facebook validado exitosamente para usuario: {}", response.get("email"));
                
                return new SocialUserInfo(
                    (String) response.get("id"),
                    (String) response.get("email"),
                    (String) response.get("name"),
                    pictureUrl
                );
            })
            .doOnError(error -> logger.error("Error validando token de Facebook", error));
    }

    /**
     * Valida token de Apple usando JWT decoding
     * Nota: Para una validación completa en producción, deberías verificar
     * la firma usando las claves públicas de Apple desde https://appleid.apple.com/auth/keys
     */
    private Mono<SocialUserInfo> validateAppleToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                // Decodificar el JWT sin verificar la firma (solo para desarrollo)
                // En producción, deberías verificar la firma con las claves públicas de Apple
                DecodedJWT jwt = JWT.decode(token);
                
                String userId = jwt.getSubject();
                String email = jwt.getClaim("email").asString();
                String name = jwt.getClaim("name").asString();
                
                // Verificar campos obligatorios
                if (userId == null || userId.isEmpty()) {
                    throw new RuntimeException("Token de Apple inválido: falta el subject (user ID)");
                }
                
                if (email == null || email.isEmpty()) {
                    throw new RuntimeException("Token de Apple inválido: falta el email");
                }
                
                // Verificar que el token no haya expirado
                if (jwt.getExpiresAt() != null && jwt.getExpiresAt().before(new java.util.Date())) {
                    throw new RuntimeException("Token de Apple expirado");
                }
                
                logger.info("Token de Apple validado exitosamente para usuario: {}", email);
                
                return new SocialUserInfo(userId, email, name, null);
                
            } catch (JWTVerificationException e) {
                logger.error("Error validando token de Apple", e);
                throw new RuntimeException("Token de Apple inválido: " + e.getMessage());
            }
        });
    }

    /**
     * Información del usuario extraída del token social
     */
    public record SocialUserInfo(
        String id,
        String email,
        String name,
        String profileImageUrl
    ) {}
}