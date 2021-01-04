package com.quorum.tessera.q2t;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.privacygroup.publish.PrivacyGroupPublisher;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class RestPrivacyGroupPublisherFactoryTest {

    private AsyncRestPrivacyGroupPublisherFactory factory;

    @Before
    public void onSetUp() {
        factory = new AsyncRestPrivacyGroupPublisherFactory();
    }

    @Test
    public void create() {

        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setCommunicationType(CommunicationType.REST);
        serverConfig.setApp(AppType.P2P);
        serverConfig.setServerAddress("http://someaddeess");
        config.setServerConfigs(Arrays.asList(serverConfig));

        PrivacyGroupPublisher publisher = factory.create(config);
        assertThat(publisher).isExactlyInstanceOf(AsyncRestPrivacyGroupPublisher.class);
    }
}
