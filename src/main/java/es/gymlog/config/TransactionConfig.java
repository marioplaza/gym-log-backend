package es.gymlog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import io.r2dbc.spi.ConnectionFactory;

/**
 * Configuración para la gestión de transacciones reactivas.
 */
@Configuration
public class TransactionConfig {

    /**
     * Crea un bean para el gestor de transacciones reactivas.
     *
     * @param connectionFactory La fábrica de conexiones R2DBC.
     * @return El gestor de transacciones.
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    /**
     * Crea un bean para el TransactionalOperator, que permite el manejo de
     * transacciones de forma programática.
     *
     * @param transactionManager El gestor de transacciones reactivas.
     * @return El operador transaccional.
     */
    @Bean
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }
}
