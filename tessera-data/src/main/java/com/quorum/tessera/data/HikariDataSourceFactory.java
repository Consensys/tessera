package com.quorum.tessera.data;

import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.util.EncryptedStringResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

enum HikariDataSourceFactory implements DataSourceFactory {
    INSTANCE;

    private DataSource dataSource;

    @Override
    public DataSource create(JdbcConfig config) {
        if(dataSource != null) {
            return dataSource;
        }

        final EncryptedStringResolver resolver = new EncryptedStringResolver();
        String url = resolver.resolve(config.getUrl());

        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        dataSource = new HikariDataSource(hikariConfig);

        return dataSource;
    }

    protected void clear() {
        dataSource = null;
    }
}
