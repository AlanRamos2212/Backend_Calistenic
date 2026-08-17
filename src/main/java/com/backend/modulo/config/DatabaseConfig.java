package com.backend.modulo.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Bean
    @Primary
    public DataSource dataSource() {
        String rawUrl = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_URL"),
                System.getenv("DATABASE_URL"),
                System.getenv("JDBC_DATABASE_URL"),
                "jdbc:postgresql://localhost:5432/calistenicdb"
        );

        String jdbcUrl = normalizeJdbcUrl(rawUrl);
        String username = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_USERNAME"),
                System.getenv("PGUSER"),
                "calistenic"
        );
        String password = firstNonBlank(
                System.getenv("SPRING_DATASOURCE_PASSWORD"),
                System.getenv("PGPASSWORD"),
                "calistenic_pass"
        );

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(1);
        dataSource.setInitializationFailTimeout(-1);
        return dataSource;
    }

    private String normalizeJdbcUrl(String value) {
        if (value == null || value.isBlank()) {
            return "jdbc:postgresql://localhost:5432/calistenicdb";
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("jdbc:postgresql://")) {
            return trimmed;
        }

        if (trimmed.startsWith("postgresql://")) {
            return "jdbc:" + trimmed;
        }

        if (trimmed.startsWith("postgres://")) {
            return "jdbc:postgresql://" + trimmed.substring("postgres://".length());
        }

        return trimmed;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
