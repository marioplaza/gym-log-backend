package es.gymlog.config;

import com.nimbusds.jwt.JWTParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private final SecurityProperties securityProperties;

    public SecurityConfig(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, ReactiveJwtDecoder jwtDecoder) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/swagger-ui.html", "/v3/api-docs/**", "/webjars/**", "/favicon.ico").permitAll()
                .pathMatchers(HttpMethod.GET, "/management/health").permitAll()
                .pathMatchers("/management/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))
            .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoderByIssuerUri() {
        Map<String, ReactiveJwtDecoder> decoders = new HashMap<>();
        securityProperties.issuerUris().forEach(issuer -> decoders.put(issuer, ReactiveJwtDecoders.fromIssuerLocation(issuer)));

        return token -> {
            try {
                String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
                ReactiveJwtDecoder decoder = decoders.get(issuer);
                if (decoder != null) {
                    return decoder.decode(token);
                } else {
                    return Mono.error(new BadJwtException("Unknown issuer: " + issuer));
                }
            } catch (ParseException e) {
                return Mono.error(new BadJwtException("Failed to parse token", e));
            }
        };
    }
}
