package com.quorum.tessera.data;

import com.quorum.tessera.config.JdbcConfig;
import java.util.ServiceLoader;
import javax.sql.DataSource;

public interface DataSourceFactory {

  DataSource create(JdbcConfig config);

  static DataSourceFactory create() {
    return ServiceLoader.load(DataSourceFactory.class).findFirst().get();
  }
}
