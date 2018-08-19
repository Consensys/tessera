package com.quorum.tessera.config;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcConfigTest {

    @Test
    public void testGetDriverClassName() {
        JdbcConfig config = new JdbcConfig("sa","sa","jdbc:sqlite::memory:", "org.sqlite.JDBC");
        assertThat(config.getDriverClassPath()).isEqualTo("org.sqlite.JDBC");
    }

    @Test
    public void testGetDefaultDriverClassName() {
        JdbcConfig config = new JdbcConfig("sa","sa","jdbc:sqlite::memory:", null);
        assertThat(config.getUrl()).isEqualTo("jdbc:sqlite::memory:");
        assertThat(config.getUsername()).isEqualTo("sa");
        assertThat(config.getPassword()).isEqualTo("sa");
        assertThat(config.getDriverClassPath()).isEqualTo("org.h2.Driver");
    }

    @Test
    public void testEmptyDriverClassName() {
        JdbcConfig config = new JdbcConfig("sa","sa","jdbc:sqlite::memory:", "");
        assertThat(config.getUrl()).isEqualTo("jdbc:sqlite::memory:");
        assertThat(config.getUsername()).isEqualTo("sa");
        assertThat(config.getPassword()).isEqualTo("sa");
        assertThat(config.getDriverClassPath()).isEqualTo("org.h2.Driver");
    }
}
