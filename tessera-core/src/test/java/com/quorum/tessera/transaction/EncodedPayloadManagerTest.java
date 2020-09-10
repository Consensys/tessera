package com.quorum.tessera.transaction;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.JdbcConfig;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EncodedPayloadManagerTest {

    @Test
    public void createFromConfig() {
        final Config config = mock(Config.class);

        final ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        final JdbcConfig jdbcConfig = new JdbcConfig("junit", "juint", "jdbc:h2:mem:junit");
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        final EncodedPayloadManager encodedPayloadManager = EncodedPayloadManager.create(config);
        assertThat(encodedPayloadManager).isNotNull();
    }
}
