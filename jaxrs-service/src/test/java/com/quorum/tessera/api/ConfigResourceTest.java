package com.quorum.tessera.api;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mockito;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ConfigResourceTest {

    private ConfigResource configResource;

    private ConfigService configService;

    @Before
    public void onSetUp() {
        configService = mock(ConfigService.class);
        configResource = new ConfigResource(configService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(configService);
    }

    @Test
    public void addPeerIsSucessful() {

        List<Peer> peers = new ArrayList<>();
        Mockito.doAnswer((inv) -> {
            peers.add(new Peer(inv.getArgument(0)));
            return null;
        }).when(configService).addPeer(anyString());
        when(configService.getPeers()).thenReturn(peers);

        Peer peer = new Peer("junit");

        Response response = configResource.addPeer(peer);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getLocation().toString()).isEqualTo("config/peers/0");
        assertThat(peers).containsExactly(peer);
        verify(configService).addPeer(peer.getUrl());
        verify(configService).getPeers();
    }

    @Test
    public void getPeerIsSucessful() {
        Peer peer = new Peer("getPeerIsSucessfulUrl");
        when(configService.getPeers()).thenReturn(Arrays.asList(peer));

        Response response = configResource.getPeer(0);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(peer);

        verify(configService).getPeers();

    }

    @Test
    public void getPeerNotFound() {
        Peer peer = new Peer("getPeerNoptFound");
        when(configService.getPeers()).thenReturn(Arrays.asList(peer));

        try {
            configResource.getPeer(2);
            failBecauseExceptionWasNotThrown(NotFoundException.class);
        } catch (NotFoundException ex) {
            verify(configService).getPeers();
        }
    }
}
