package es.gymlog.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Representa un rol de usuario en la aplicación.
 */
@Table("roles")
public record Role(
    @Id Integer id,
    String name
) {}
