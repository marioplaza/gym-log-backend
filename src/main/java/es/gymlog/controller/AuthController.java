package es.gymlog.controller;

import es.gymlog.api.AuthApi;
import es.gymlog.api.dto.AuthResponseDTO;
import es.gymlog.api.dto.SocialLoginDTO;
import es.gymlog.api.dto.UserDTO;
import es.gymlog.mapper.UserMapper;
import es.gymlog.service.SocialTokenValidationService;
import es.gymlog.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
public class AuthController implements AuthApi {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtEncoder jwtEncoder;
    private final UserService userService;
    private final UserMapper userMapper;
    private final SocialTokenValidationService socialTokenValidationService;

    public AuthController(JwtEncoder jwtEncoder, 
                         UserService userService, 
                         UserMapper userMapper,
                         SocialTokenValidationService socialTokenValidationService) {
        this.jwtEncoder = jwtEncoder;
        this.userService = userService;
        this.userMapper = userMapper;
        this.socialTokenValidationService = socialTokenValidationService;
    }

    @Override
    public Mono<ResponseEntity<UserDTO>> getCurrentUser(ServerWebExchange exchange) {
        return ReactiveSecurityContextHolder.getContext()
            .cast(org.springframework.security.core.context.SecurityContext.class)
            .map(securityContext -> securityContext.getAuthentication())
            .cast(JwtAuthenticationToken.class)
            .map(jwtAuthenticationToken -> {
                Jwt jwt = jwtAuthenticationToken.getToken();
                UserDTO userDTO = new UserDTO();
                userDTO.id(UUID.fromString(jwt.getSubject()));
                userDTO.email(jwt.getClaim("email"));
                userDTO.name(jwt.getClaim("name"));
                // El resto de campos no se incluyen en el token por simplicidad
                return ResponseEntity.ok(userDTO);
            });
    }

    @Override
    public Mono<ResponseEntity<AuthResponseDTO>> socialLogin(Mono<SocialLoginDTO> socialLoginDTOMono, ServerWebExchange exchange) {
        return socialLoginDTOMono
            .flatMap(socialLoginDTO -> {
                String provider = socialLoginDTO.getProvider().getValue();
                String token = socialLoginDTO.getToken();
                
                logger.info("Iniciando validación de token social para proveedor: {}", provider);
                
                // Validar el token social real
                return socialTokenValidationService.validateToken(provider, token)
                    .flatMap(socialUserInfo -> {
                        logger.info("Token social validado exitosamente para usuario: {}", socialUserInfo.email());
                        
                        // Buscar o crear usuario en la base de datos
                        return userService.findOrCreateUserFromSocial(
                            provider, 
                            socialUserInfo.id(), 
                            socialUserInfo.email(), 
                            socialUserInfo.name()
                        );
                    })
                    .flatMap(user -> {
                        // Generar JWT propio de la aplicación
                        JwtClaimsSet claims = JwtClaimsSet.builder()
                            .issuer("gymlog")
                            .subject(user.id().toString())
                            .claim("email", user.email())
                            .claim("name", user.displayName())
                            .issuedAt(Instant.now())
                            .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS)) // 24 horas de expiración
                            .build();

                        // Como JwtEncoder no es reactivo, usamos Mono.fromCallable
                        return Mono.fromCallable(() -> jwtEncoder.encode(JwtEncoderParameters.from(claims)))
                            .map(jwt -> {
                                logger.info("JWT generado exitosamente para usuario: {}", user.email());
                                
                                AuthResponseDTO authResponse = new AuthResponseDTO();
                                authResponse.jwt(jwt.getTokenValue());
                                authResponse.user(userMapper.toDto(user));
                                return ResponseEntity.ok(authResponse);
                            });
                    });
            })
            .onErrorMap(throwable -> {
                logger.error("Error durante el proceso de login social", throwable);
                return new ResponseStatusException(UNAUTHORIZED, "Token social inválido: " + throwable.getMessage());
            });
    }
}