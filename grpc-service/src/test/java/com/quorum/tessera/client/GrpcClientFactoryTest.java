package com.quorum.tessera.client;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GrpcClientFactoryTest {


    
    @Test
    public void testCreateNewClient() {
        GrpcClientFactory grpcClientFactory = new GrpcClientFactory();
        
        GrpcClient client1 = grpcClientFactory.getClient("bogus.com");
        assertThat(client1).isNotNull();
        GrpcClient client2 = grpcClientFactory.getClient("morebogus.com");
        assertThat(client2).isNotNull();

        assertThat(grpcClientFactory.getClient("bogus.com")).isEqualTo(client1);
        assertThat(grpcClientFactory.getClient("morebogus.com")).isEqualTo(client2);
    }
}
