package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.api.model.ResendRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

public class RestP2pClientTest {

    private static final URI TARGET = URI.create("someuri.com");

    private PostDelegate postDelegate;

    private RestP2pClient p2pClient;

    @Before
    public void onSetUp() {
        this.postDelegate = mock(PostDelegate.class);

        this.p2pClient = new RestP2pClient(postDelegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(postDelegate);
    }

    @Test
    public void getPartyInfo() {
        final byte[] someData = "somedata".getBytes();

        p2pClient.getPartyInfo(TARGET, someData);

        verify(postDelegate).doPost(TARGET, ApiPath.PARTYINFO, someData);
    }

    @Test
    public void makeResendRequest() {
        final ResendRequest request = mock(ResendRequest.class);

        p2pClient.makeResendRequest(TARGET, request);

        verify(postDelegate).makeResendRequest(TARGET, request);
    }

    @Test
    public void push() {
        final byte[] someData = "somedata".getBytes();

        p2pClient.push(TARGET, someData);

        verify(postDelegate).doPost(TARGET, ApiPath.PUSH, someData);
    }
}
