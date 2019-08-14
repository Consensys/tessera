package com.quorum.tessera.client;

import com.quorum.tessera.partyinfo.PushBatchRequest;
import com.quorum.tessera.partyinfo.ResendBatchRequest;
import com.quorum.tessera.partyinfo.ResendRequest;
import com.quorum.tessera.partyinfo.ResendRequestType;

import static org.assertj.core.api.Assertions.assertThat;

import com.quorum.tessera.grpc.p2p.ResendBatchResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class GrpcP2pClientTest {

    private GrpcP2pClient p2pClient;

    private GrpcClientFactory grpcClientFactory;

    private GrpcClient grpcClient;

    private final String targetUrl = "someurl";

    public GrpcP2pClientTest() {}

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
        p2pClient.sendPartyInfo(targetUrl, data);
        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).sendPartyInfo(data);
    }

    @Test
    public void push() {
        byte[] data = "DATA".getBytes();
        p2pClient.push(targetUrl, data);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).push(data);
    }

    @Test
    public void pushBatch() {
        byte[] data = "DATA".getBytes();
        PushBatchRequest request = new PushBatchRequest(Collections.singletonList(data));
        p2pClient.pushBatch(targetUrl, request);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).pushBatch(any());
    }

    @Test
    public void makeResendRequestAll() {
        byte[] data = "DATA".getBytes();

        ResendRequest resendRequest = new ResendRequest();
        resendRequest.setKey("KEY");
        resendRequest.setPublicKey("PUBLICKEY");
        resendRequest.setType(ResendRequestType.ALL);

        when(grpcClient.makeResendRequest(any())).thenReturn(true);

        p2pClient.makeResendRequest(targetUrl, resendRequest);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).makeResendRequest(any());
    }

    @Test
    public void makeResendBatchRequest() {

        ResendBatchRequest resendRequest = new ResendBatchRequest();
        resendRequest.setPublicKey("PUBLICKEY");
        resendRequest.setBatchSize(100);
        ResendBatchResponse resendBatchResponse = ResendBatchResponse.newBuilder().setTotal(100).build();

        when(grpcClient.makeBatchResendRequest(any())).thenReturn(resendBatchResponse);

        p2pClient.makeBatchResendRequest(targetUrl, resendRequest);

        verify(grpcClientFactory).getClient(targetUrl);
        verify(grpcClient).makeBatchResendRequest(any());
    }

    @Test
    public void defaultConstuct() {
        GrpcP2pClient instance = new GrpcP2pClient();
        assertThat(instance).isNotNull();
    }
}
