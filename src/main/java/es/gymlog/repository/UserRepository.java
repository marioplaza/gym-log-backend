package es.gymlog.repository;

import es.gymlog.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio para la gestión de datos de la entidad User.
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByProviderAndProviderId(String provider, String providerId);
}
