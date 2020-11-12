package com.quorum.tessera.data;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DataSourceFactoryTest {

    @Test
    public void createFactory() {
        assertThat(DataSourceFactory.create())
            .isNotNull()
            .isExactlyInstanceOf(HikariDataSourceFactory.class);
    }

}
