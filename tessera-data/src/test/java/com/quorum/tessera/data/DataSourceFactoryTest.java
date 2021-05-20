package com.quorum.tessera.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.data.internal.HikariDataSourceFactory;
import org.junit.Test;

public class DataSourceFactoryTest {

  @Test
  public void createFactory() {
    assertThat(DataSourceFactory.create())
        .isNotNull()
        .isExactlyInstanceOf(HikariDataSourceFactory.class);
  }
}
