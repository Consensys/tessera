package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.MockContextHolder;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AutoDiscoveryTest {

    private AutoDiscovery discovery;

    private NetworkStore networkStore;


    private RuntimeContext runtimeContext;

    @Before
    public void onSetUp() {
        networkStore = mock(NetworkStore.class);

        discovery = new AutoDiscovery(networkStore);


        runtimeContext = RuntimeContext.getInstance();
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(networkStore,runtimeContext);
        MockContextHolder.reset();
    }



    @Test
    public void onUpdateIgnoresKeysThatAreNotOwnedBySender() {

        String uri = "http://mynode.com";

        when(runtimeContext.getP2pServerUri()).thenReturn(URI.create(uri));

        PublicKey key = mock(PublicKey.class);
        Recipient recipient = Recipient.of(key,uri);
        Recipient other = Recipient.of(mock(PublicKey.class),"http://othernode.com");
        List<Recipient> recipients = List.of(recipient,other);

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(recipients)
            .withSupportedApiVersions(List.of("Two","Fifty"))
            .build();

        List<ActiveNode> storedNodes = new ArrayList<>();
        doAnswer(invocation -> {
            storedNodes.add(invocation.getArgument(0));
            return null;
        }).when(networkStore).store(any(ActiveNode.class));

        discovery.onUpdate(nodeInfo);

        assertThat(storedNodes).hasSize(1);

        ActiveNode result = storedNodes.iterator().next();

        assertThat(result.getUri()).isEqualTo(NodeUri.create(uri));
        assertThat(result.getKeys()).containsExactly(key);
        assertThat(result.getSupportedVersions()).containsExactlyInAnyOrder("Two","Fifty");
        verify(networkStore).store(any(ActiveNode.class));
        verify(runtimeContext).getP2pServerUri();

    }

    @Test
    public void onUpdateSendHasTwoKeys() {

        String uri = "http://mynode.com";
        PublicKey key = mock(PublicKey.class);
        Recipient recipient = Recipient.of(key,uri);
        PublicKey otherKey = mock(PublicKey.class);
        Recipient other = Recipient.of(otherKey,uri);

        when(runtimeContext.getP2pServerUri()).thenReturn(URI.create(uri));

        List<Recipient> recipients = List.of(recipient,other);

        NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl(uri)
            .withRecipients(recipients)
            .build();

        List<ActiveNode> storedNodes = new ArrayList<>();
        doAnswer(invocation -> {
            storedNodes.add(invocation.getArgument(0));
            return null;
        }).when(networkStore).store(any(ActiveNode.class));

        discovery.onUpdate(nodeInfo);

        assertThat(storedNodes).hasSize(1);

        ActiveNode result = storedNodes.iterator().next();

        assertThat(result.getUri()).isEqualTo(NodeUri.create(uri));
        assertThat(result.getKeys()).containsExactlyInAnyOrder(key,otherKey);

        verify(networkStore).store(any(ActiveNode.class));
        verify(runtimeContext).getP2pServerUri();
    }

    @Test
    public void onDisconnect() {
        URI uri = URI.create("http://onDisconnect.com");
        List<NodeUri> results = new ArrayList<>();
        doAnswer(invocation -> {
            results.add(invocation.getArgument(0));
            return null;
        }).when(networkStore).remove(any(NodeUri.class));

        discovery.onDisconnect(uri);

        assertThat(results).containsExactly(NodeUri.create(uri));
        verify(networkStore).remove(any(NodeUri.class));

    }

}
