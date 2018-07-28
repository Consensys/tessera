package com.quorum.tessera.config.builder;

import com.quorum.tessera.config.JdbcConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JdbcConfigFactoryTest {

    @Test
    public void sqllite() {

        JdbcConfig jdbcConfig = JdbcConfigFactory.fromLegacyStorageString("sqlite:somepath");

        assertThat(jdbcConfig.getUrl())
                .isEqualTo("jdbc:sqlite:somepath");
        assertThat(jdbcConfig.getUsername()).isNull();
        assertThat(jdbcConfig.getPassword()).isNull();

    }

    @Test
    public void memory() {

        JdbcConfig jdbcConfig = JdbcConfigFactory.fromLegacyStorageString("memory");

        assertThat(jdbcConfig.getUrl())
                .isEqualTo("jdbc:h2:mem:tessera");
        assertThat(jdbcConfig.getUsername()).isNull();
        assertThat(jdbcConfig.getPassword()).isNull();

    }

    @Test(expected = IllegalArgumentException.class)
    public void nullIsNotAllowed() {
        JdbcConfigFactory.fromLegacyStorageString(null);

    }

    @Test
    public void jdbcUrlsAreLeftUntouched() {
        JdbcConfig jdbcConfig = JdbcConfigFactory.fromLegacyStorageString("jdbc:somedb:somepath");

        assertThat(jdbcConfig.getUrl()).isEqualTo("jdbc:somedb:somepath");
        assertThat(jdbcConfig.getUsername()).isNull();
        assertThat(jdbcConfig.getPassword()).isNull();
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void unknownIsNotSupported() {
        JdbcConfigFactory.fromLegacyStorageString("unknown");
    }


}
