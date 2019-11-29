package com.quorum.tessera.partyinfo;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class PayloadPublisherFactoryTest {

    @Test
    public void createFactoryAndThenPublisher() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setEnabled(true);
        serverConfig.setCommunicationType(CommunicationType.REST);
        config.setServerConfigs(Arrays.asList(serverConfig));

        PayloadPublisherFactory factory = PayloadPublisherFactory.newFactory(config);

        assertThat(factory.communicationType()).isEqualByComparingTo(CommunicationType.REST);

        PayloadPublisher payloadPublisher = factory.create(config);

        assertThat(payloadPublisher).isNotNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createFactoryAndThenPublisherNoFactoryFound() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setEnabled(true);
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        config.setServerConfigs(Arrays.asList(serverConfig));

        PayloadPublisherFactory.newFactory(config);
    }
}
