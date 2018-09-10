package com.quorum.tessera.client;

import com.quorum.tessera.api.model.ApiPath;
import com.quorum.tessera.api.model.ResendRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.*;

public class RestP2pClientTest {

    private PostDelegate postDelegate;

    private RestP2pClient p2pClient;

    @Before
    public void onSetUp() {
        postDelegate = mock(PostDelegate.class);
        p2pClient = new RestP2pClient(postDelegate);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(postDelegate);
    }

    @Test
    public void getPartyInfo() {
        String url = "someurl";
        byte[] someData = "somedata".getBytes();

        p2pClient.getPartyInfo(url, someData);

        verify(postDelegate).doPost(url, ApiPath.PARTYINFO, someData);
    }

    @Test
    public void makeResendRequest() {
        String url = "someurl";
        ResendRequest request = mock(ResendRequest.class);
        p2pClient.makeResendRequest(url, request);

        verify(postDelegate).makeResendRequest(url, request);
    }

    @Test
    public void push() {
        String url = "someurl";
        byte[] someData = "somedata".getBytes();

        p2pClient.push(url, someData);

        verify(postDelegate).doPost(url, ApiPath.PUSH, someData);
    }
}
