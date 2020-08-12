package com.quorum.tessera.p2p;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class RestNodeInfoPublisherFactoryTest {

    private RestNodeInfoPublisherFactory factory;

    @Before
    public void onSetUp() {
        factory = new RestNodeInfoPublisherFactory();
    }

    @Test
    public void communicationTypeIsRest() {
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

        NodeInfoPublisher publisher = factory.create(config);
        assertThat(publisher).isExactlyInstanceOf(RestNodeInfoPublisher.class);
    }
}
