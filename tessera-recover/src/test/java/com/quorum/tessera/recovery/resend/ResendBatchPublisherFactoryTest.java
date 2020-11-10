package com.quorum.tessera.recovery.resend;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.recovery.MockResendBatchPublisherFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ResendBatchPublisherFactoryTest {

    @Ignore
    @Test
    public void createFactoryAndThenPublisher() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.REST);
        config.setServerConfigs(Arrays.asList(serverConfig));

        ResendBatchPublisherFactory factory = ResendBatchPublisherFactory.newFactory(config);

        assertThat(factory.communicationType()).isEqualByComparingTo(CommunicationType.REST);
        assertThat(factory).isExactlyInstanceOf(MockResendBatchPublisherFactory.class);

        ResendBatchPublisher payloadPublisher = factory.create(config);

        assertThat(payloadPublisher).isNotNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createFactoryAndThenPublisherNoFactoryFound() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        config.setServerConfigs(Arrays.asList(serverConfig));

        ResendBatchPublisherFactory.newFactory(config);
    }
}
