package com.quorum.tessera.q2t;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.transaction.publish.PayloadPublisher;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
public class RestPayloadPublisherFactoryTest {

    private RestPayloadPublisherFactory factory;

    @Before
    public void onSetUp() {
        factory = new RestPayloadPublisherFactory();
        assertThat(factory.communicationType()).isEqualTo(CommunicationType.REST);
    }

    @Test
    public void create() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setApp(AppType.P2P);
        serverConfig.setServerAddress("http://someaddeess");
        config.setServerConfigs(Arrays.asList(serverConfig));

        PayloadPublisher payloadPublisher = factory.create(config);
        assertThat(payloadPublisher).isExactlyInstanceOf(RestPayloadPublisher.class);
    }
}
