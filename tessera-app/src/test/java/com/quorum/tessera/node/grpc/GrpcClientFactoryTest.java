package com.quorum.tessera.node.grpc;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcClientFactoryTest {


    @Test
    public void testCreateNewClient() {
        GrpcClient client1 = GrpcClientFactory.getClient("bogus.com");
        assertThat(client1).isNotNull();
        GrpcClient client2 = GrpcClientFactory.getClient("morebogus.com");
        assertThat(client2).isNotNull();

        assertThat(GrpcClientFactory.getClient("bogus.com")).isEqualTo(client1);
        assertThat(GrpcClientFactory.getClient("morebogus.com")).isEqualTo(client2);
    }
}
