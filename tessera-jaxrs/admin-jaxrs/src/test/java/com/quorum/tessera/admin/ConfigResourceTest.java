package com.quorum.tessera.admin;

import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.partyinfo.model.PartyInfo;
import com.quorum.tessera.config.Peer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ConfigResourceTest {

    private ConfigResource configResource;

    private ConfigService configService;

    private PartyInfoService partyInfoService;

    @Before
    public void onSetUp() {
        configService = mock(ConfigService.class);
        partyInfoService = mock(PartyInfoService.class);
        configResource = new ConfigResource(configService, partyInfoService);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(configService, partyInfoService);
    }

    @Test
    public void addPeerIsSuccessful() {

        final Peer peer = new Peer("junit");
        final List<Peer> peers = new ArrayList<>();

        Mockito.doAnswer(
                        inv -> {
                            peers.add(new Peer(inv.getArgument(0)));
                            return null;
                        })
                .when(configService)
                .addPeer(anyString());
        when(configService.getPeers()).thenReturn(peers);

        final Response response = configResource.addPeer(peer);

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getLocation().toString()).isEqualTo("config/peers/0");
        assertThat(peers).containsExactly(peer);

        verify(configService).addPeer(peer.getUrl());
        verify(configService, times(2)).getPeers();
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
    }

    @Test
    public void addExistingPeerIsSuccessful() {

        final Peer peer = new Peer("junit");
        final List<Peer> peers = new ArrayList<>();

        Mockito.doAnswer(
                        inv -> {
                            peers.add(new Peer(inv.getArgument(0)));
                            return null;
                        })
                .when(configService)
                .addPeer(anyString());
        when(configService.getPeers()).thenReturn(peers);

        final Response responseOne = configResource.addPeer(peer);
        assertThat(responseOne.getStatus()).isEqualTo(201);
        assertThat(responseOne.getLocation().toString()).isEqualTo("config/peers/0");
        assertThat(peers).containsExactly(peer);

        final Response responseTwo = configResource.addPeer(peer);
        assertThat(responseTwo.getStatus()).isEqualTo(200);
        assertThat(responseTwo.getLocation().toString()).isEqualTo("config/peers/0");
        assertThat(peers).containsExactly(peer);

        verify(configService).addPeer(peer.getUrl());
        verify(configService, times(4)).getPeers();
        verify(partyInfoService).updatePartyInfo(any(PartyInfo.class));
    }

    @Test
    public void getPeerIsSuccessful() {
        final Peer peer = new Peer("getPeerIsSucessfulUrl");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final Response response = configResource.getPeer(0);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(peer);

        verify(configService).getPeers();
    }

    @Test
    public void getPeerNotFound() {
        final Peer peer = new Peer("getPeerNotFound");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final Throwable throwable = catchThrowable(() -> this.configResource.getPeer(2));

        assertThat(throwable).isInstanceOf(NotFoundException.class);

        verify(configService).getPeers();
    }

    @Test
    public void getPeers() {
        final Peer peer = new Peer("somepeer");
        when(configService.getPeers()).thenReturn(singletonList(peer));

        final Response response = configResource.getPeers();

        assertThat(response.getStatus()).isEqualTo(200);

        List<Peer> results = (List<Peer>) response.getEntity();
        assertThat(results).containsExactly(peer);

        verify(configService).getPeers();
    }
}
