package com.quorum.tessera.data.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.data.DataSourceFactory;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HikariDataSourceFactoryTest {

  private DataSourceFactory dataSourceFactory;

  @Before
  public void beforeTest() {
    dataSourceFactory = HikariDataSourceFactory.INSTANCE;
  }

  @After
  public void clear() {
    HikariDataSourceFactory.class.cast(dataSourceFactory).clear();
  }

  @Test
  public void create() {

    String username = "junit";
    String password = "junitpw";
    String url = "jdbc:h2:mem:";

    JdbcConfig jdbcConfig = mock(JdbcConfig.class);
    when(jdbcConfig.getUsername()).thenReturn(username);
    when(jdbcConfig.getPassword()).thenReturn(password);
    when(jdbcConfig.getUrl()).thenReturn(url);

    DataSource dataSource = dataSourceFactory.create(jdbcConfig);

    assertThat(dataSource).isNotNull().isExactlyInstanceOf(HikariDataSource.class);

    HikariDataSource hikariDataSource = HikariDataSource.class.cast(dataSource);
    assertThat(hikariDataSource.getJdbcUrl()).isEqualTo(url);
    assertThat(hikariDataSource.getUsername()).isEqualTo(username);
    assertThat(hikariDataSource.getPassword()).isEqualTo(password);

    assertThat(dataSource)
        .describedAs("Second call returns same instance")
        .isSameAs(dataSourceFactory.create(jdbcConfig));
  }
}
