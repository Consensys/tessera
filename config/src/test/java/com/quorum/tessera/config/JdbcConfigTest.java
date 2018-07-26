package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcConfigTest {

    @Test
    public void testGetDriverClassName() {
        JdbcConfig config = new JdbcConfig("sa","sa","jdbc:sqlite::memory:");
        assertThat(config.getDriverClassName()).isEqualTo("org.sqlite.JDBC");
    }
}
