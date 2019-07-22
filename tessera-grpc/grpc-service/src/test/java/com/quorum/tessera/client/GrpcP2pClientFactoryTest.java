package com.quorum.tessera.client;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;

public class GrpcP2pClientFactoryTest {

    @Test
    public void create() {

        com.quorum.tessera.client.GrpcP2pClientFactory grpcP2pClientFactory = new com.quorum.tessera.client.GrpcP2pClientFactory();

        assertThat(grpcP2pClientFactory.communicationType()).isEqualTo(CommunicationType.GRPC);
        Config config = mock(Config.class);

        P2pClient result = grpcP2pClientFactory.create(config);

        assertThat(result).isExactlyInstanceOf(GrpcP2pClient.class);

    }

    @Test
    public void createWithNullConfig() {

        GrpcP2pClientFactory grpcP2pClientFactory = new GrpcP2pClientFactory();

        assertThat(grpcP2pClientFactory.communicationType()).isEqualTo(CommunicationType.GRPC);
        P2pClient result = grpcP2pClientFactory.create(null);

        assertThat(result).isExactlyInstanceOf(GrpcP2pClient.class);

    }

}
