package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.data.DataSourceFactory;
import java.util.ServiceLoader;
import org.junit.Test;

public class DataSourceFactoryProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new DataSourceFactoryProvider()).isNotNull();
  }

  @Test
  public void provider() {
    DataSourceFactory dataSourceFactory = DataSourceFactoryProvider.provider();
    assertThat(dataSourceFactory).isNotNull().isExactlyInstanceOf(HikariDataSourceFactory.class);
  }

  @Test
  public void loadFromModuleInfo() {
    DataSourceFactory dataSourceFactory =
        ServiceLoader.load(DataSourceFactory.class).findFirst().get();
    assertThat(dataSourceFactory).isNotNull().isExactlyInstanceOf(HikariDataSourceFactory.class);
  }
}
