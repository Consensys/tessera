package com.quorum.tessera.api;

import com.quorum.tessera.config.Peer;
import com.quorum.tessera.core.config.ConfigService;
import java.util.Arrays;
import javax.ws.rs.core.Response;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
        Peer peer = new Peer("junit");

        Response response = configResource.addPeer(peer);
        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getLocation().toString()).isEqualTo("/peers/junit");

        verify(configService).addPeer(peer.getUrl());
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

}
