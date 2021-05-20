package com.quorum.tessera.data.internal;

import com.quorum.tessera.data.DataSourceFactory;

public class DataSourceFactoryProvider {

  public static DataSourceFactory provider() {
    return HikariDataSourceFactory.INSTANCE;
  }
}
