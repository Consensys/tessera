package com.quorum.tessera.dao;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

public class JpaSqliteConfig extends JpaConfig {

    @Bean
    public DataSource dataSource() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite::memory:");
        dataSource.setUsername("sa");
        return dataSource;
    }
}
