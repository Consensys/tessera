package com.quorum.tessera.client;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.node.PostDelegate;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class P2pClientFactoryTest {

    @Test
    public void createForGrpc() {
        P2pClientFactory p2pClientFactory = new P2pClientFactory(null, CommunicationType.GRPC);
        P2pClient client = p2pClientFactory.create();

        assertThat(client).isExactlyInstanceOf(GrpcP2pClient.class);
    }

    @Test
    public void createForRest() {
        PostDelegate postDelegate = mock(PostDelegate.class);
        P2pClientFactory p2pClientFactory = new P2pClientFactory(postDelegate, CommunicationType.REST);
        P2pClient client = p2pClientFactory.create();

        assertThat(client).isExactlyInstanceOf(RestP2pClient.class);
    }

    @Test(expected = NullPointerException.class)
    public void createForRestNullDelegate() {
        P2pClientFactory p2pClientFactory = new P2pClientFactory(null, CommunicationType.REST);
        p2pClientFactory.create();
    }

}
