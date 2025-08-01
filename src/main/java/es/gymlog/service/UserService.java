package es.gymlog.service;

import es.gymlog.model.User;
import es.gymlog.repository.UserRepository;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<User> findOrCreateUser(OAuth2AuthenticationToken authentication) {
        String provider = authentication.getAuthorizedClientRegistrationId();
        String providerId = authentication.getName();
        String email = authentication.getPrincipal().getAttribute("email");
        String displayName = authentication.getPrincipal().getAttribute("name");

        return userRepository.findByProviderAndProviderId(provider, providerId)
            .switchIfEmpty(Mono.defer(() -> {
                User newUser = new User(
                    UUID.randomUUID(),
                    providerId,
                    provider,
                    email,
                    displayName,
                    Instant.now(),
                    Instant.now()
                );
                return userRepository.save(newUser);
            }));
    }
}
