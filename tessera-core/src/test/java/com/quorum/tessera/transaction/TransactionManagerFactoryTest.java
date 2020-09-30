package com.quorum.tessera.transaction;

import com.quorum.tessera.config.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TransactionManagerFactoryTest {

    @Test
    public void create() {

        TransactionManagerFactory result = TransactionManagerFactory.create();
        assertThat(result).isNotNull();

        Config config = mock(Config.class);
        ServerConfig serverConfig = mock(ServerConfig.class);
        when(serverConfig.getCommunicationType()).thenReturn(CommunicationType.REST);
        when(config.getP2PServerConfig()).thenReturn(serverConfig);

        JdbcConfig jdbcConfig = mock(JdbcConfig.class);
        when(jdbcConfig.getUsername()).thenReturn("junit");
        when(jdbcConfig.getPassword()).thenReturn("junit");
        when(jdbcConfig.getUrl()).thenReturn("jdbc:h2:mem:junit");
        when(config.getJdbcConfig()).thenReturn(jdbcConfig);

        FeatureToggles features = mock(FeatureToggles.class);
        when(features.isEnablePrivacyEnhancements()).thenReturn(false);
        when(config.getFeatures()).thenReturn(features);

        TransactionManager transactionManager = result.create(config);
        assertThat(transactionManager).isNotNull();

        assertThat(result.create(config)).isSameAs(transactionManager);
        assertThat(result.transactionManager().get()).isSameAs(transactionManager);
    }
}
