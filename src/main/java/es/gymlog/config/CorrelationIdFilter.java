package es.gymlog.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * Filtro web para añadir un ID de correlación a cada petición.
 * Esto permite trazar una petición a través de todo el sistema en los logs.
 */
@Component
public class CorrelationIdFilter implements WebFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_KEY, correlationId);
        return chain.filter(exchange)
            .contextWrite(Context.of(CORRELATION_ID_KEY, correlationId))
            .doFinally(signalType -> MDC.remove(CORRELATION_ID_KEY));
    }
}
