package com.finanzas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

/**
 * Habilita {@code @CreatedDate} y {@code @LastModifiedDate}: Spring Data completa
 * las marcas de tiempo al guardar, en vez de que cada repositorio las setee a mano.
 */
@Configuration
@EnableJdbcAuditing
public class PersistenceConfig {
}
