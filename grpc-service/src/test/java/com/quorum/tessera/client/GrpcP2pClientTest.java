package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GrpcP2pClientTest {

    private GrpcP2pClient p2pClient;

    private GrpcClientFactory grpcClientFactory;

    private GrpcClient grpcClient;

    private final String targetUrl = "someurl";

    private final URI targetUri = URI.create(targetUrl);

    @Before
    public void setUp() {
        grpcClientFactory = mock(GrpcClientFactory.class);
        grpcClient = mock(GrpcClient.class);
        when(grpcClientFactory.getClient(targetUrl)).thenReturn(grpcClient);

        p2pClient = new GrpcP2pClient(grpcClientFactory);
    }

    @After
    public void tearDown() {
        verifyNoMoreInteractions(grpcClientFactory, grpcClient);
    }

    @Test
    public void getPartyInfo() {
        byte[] data = "DATA".getBytes();
        p2pClient.getPartyInfo(targetUri, data);
        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).getPartyInfo(data);

    }

    @Test
    public void push() {
        byte[] data = "DATA".getBytes();
        p2pClient.push(targetUri, data);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).push(data);

    }

    @Test
    public void makeResendRequestAll() {
        byte[] data = "DATA".getBytes();

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey("KEY");
        resendRequest.setPublicKey("PUBLICKEY");
        resendRequest.setType(ResendRequestType.ALL);

        when(grpcClient.makeResendRequest(any())).thenReturn(true);

        p2pClient.makeResendRequest(targetUri, resendRequest);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).makeResendRequest(any());

    }
    
}
