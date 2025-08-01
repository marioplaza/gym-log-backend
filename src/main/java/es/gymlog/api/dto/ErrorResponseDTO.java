package es.gymlog.api.dto;

/**
 * DTO para respuestas de error estandarizadas.
 * @param errorCode Un código de error único para la aplicación.
 * @param message Un mensaje descriptivo del error.
 */
public record ErrorResponseDTO(String errorCode, String message) {
}
