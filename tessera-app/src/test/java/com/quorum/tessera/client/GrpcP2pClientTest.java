package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.node.grpc.GrpcClient;
import com.quorum.tessera.node.grpc.GrpcClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GrpcP2pClientTest {

    private static final String TARGET_URL = "someurl";

    private GrpcClientFactory grpcClientFactory;

    private GrpcP2pClient p2pClient;

    private GrpcClient grpcClient;

    @Before
    public void onSetUp() throws Exception {

        grpcClient = mock(GrpcClient.class);
        grpcClientFactory = mock(GrpcClientFactory.class);
        when(grpcClientFactory.getClient(TARGET_URL)).thenReturn(grpcClient);

        p2pClient = new GrpcP2pClient(grpcClientFactory);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(grpcClientFactory, grpcClient);
    }

    @Test
    public void getPartyInfo() {

        byte[] someData = "somedata".getBytes();

        p2pClient.getPartyInfo(TARGET_URL, someData);

        verify(grpcClientFactory).getClient(TARGET_URL);
        verify(grpcClient).getPartyInfo(someData);

    }

    @Test
    public void makeResendRequest() {

        ResendRequest request = new ResendRequest();
        request.setKey("KEY");
        request.setPublicKey("PUBLIC_KEY");
        request.setType(com.quorum.tessera.api.model.ResendRequestType.INDIVIDUAL);

        p2pClient.makeResendRequest(TARGET_URL, request);

        verify(grpcClientFactory).getClient(TARGET_URL);
        verify(grpcClient).makeResendRequest(any());
    }

    @Test
    public void push() {
        byte[] someData = "somedata".getBytes();

        p2pClient.push(TARGET_URL, someData);
        verify(grpcClientFactory).getClient(TARGET_URL);
        verify(grpcClient).push(someData);
    }
}
