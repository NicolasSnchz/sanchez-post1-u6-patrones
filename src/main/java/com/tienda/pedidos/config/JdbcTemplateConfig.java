package com.tienda.pedidos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

// El codigo del sistema asume que queryForObject devuelve null cuando la consulta no
// encuentra filas (ej. cliente o producto inexistente). Por defecto JdbcTemplate lanza
// EmptyResultDataAccessException; este JdbcTemplate devuelve null en ese caso.
@Configuration
public class JdbcTemplateConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplateNullSiVacio(dataSource);
    }

    static class JdbcTemplateNullSiVacio extends JdbcTemplate {

        JdbcTemplateNullSiVacio(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) {
            try {
                return super.queryForObject(sql, requiredType, args);
            } catch (EmptyResultDataAccessException e) {
                return null;
            }
        }
    }
}
