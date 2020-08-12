package com.quorum.tessera.p2p;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.context.RuntimeContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.client.Client;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeInfoPublisherFactoryTest {

    @Before
    public void before() {
        final RuntimeContext context = RuntimeContext.getInstance();
        assertThat(Mockito.mockingDetails(context).isMock()).isTrue();

        when(context.getP2pClient()).thenReturn(mock(Client.class));
    }

    @Test
    public void createFactoryAndThenPublisher() {
        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.REST);
        config.setServerConfigs(List.of(serverConfig));

        final NodeInfoPublisherFactory factory = NodeInfoPublisherFactory.newFactory(config);

        assertThat(factory.communicationType()).isEqualByComparingTo(CommunicationType.REST);

        final NodeInfoPublisher publisher = factory.create(config);

        assertThat(publisher).isNotNull();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void createFactoryAndThenPublisherNoFactoryFound() {
        final Config config = new Config();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setApp(AppType.P2P);
        serverConfig.setCommunicationType(CommunicationType.WEB_SOCKET);
        config.setServerConfigs(List.of(serverConfig));

        NodeInfoPublisherFactory.newFactory(config);
    }
}
