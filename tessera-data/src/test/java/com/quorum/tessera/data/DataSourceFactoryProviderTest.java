package com.quorum.tessera.data;

import org.junit.Test;

import java.util.ServiceLoader;

import static org.assertj.core.api.Assertions.assertThat;

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
        DataSourceFactory dataSourceFactory = ServiceLoader.load(DataSourceFactory.class).findFirst().get();
        assertThat(dataSourceFactory).isNotNull().isExactlyInstanceOf(HikariDataSourceFactory.class);
    }

}
