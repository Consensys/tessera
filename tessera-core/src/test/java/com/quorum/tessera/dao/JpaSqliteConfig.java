package com.quorum.tessera.dao;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class JpaSqliteConfig extends JpaConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setJdbcUrl("jdbc:sqlite::memory:");
        dataSource.setUsername("sa");
        return dataSource;
    }
}
