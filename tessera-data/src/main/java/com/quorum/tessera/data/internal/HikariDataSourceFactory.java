package com.quorum.tessera.data.internal;

import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.util.EncryptedStringResolver;
import com.quorum.tessera.data.DataSourceFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public enum HikariDataSourceFactory implements DataSourceFactory {
  INSTANCE;

  private DataSource dataSource;

  @Override
  public DataSource create(JdbcConfig config) {
    if (dataSource != null) {
      return dataSource;
    }

    final EncryptedStringResolver resolver = new EncryptedStringResolver();

    final HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.getUrl());
    hikariConfig.setUsername(config.getUsername());
    hikariConfig.setPassword(resolver.resolve(config.getPassword()));

    dataSource = new HikariDataSource(hikariConfig);

    return dataSource;
  }

  protected void clear() {
    dataSource = null;
  }
}
