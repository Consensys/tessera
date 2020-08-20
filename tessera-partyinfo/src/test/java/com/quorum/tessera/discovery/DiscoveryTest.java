package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.MockContextHolder;
import com.quorum.tessera.partyinfo.node.NodeInfo;
import com.quorum.tessera.partyinfo.node.Party;
import com.quorum.tessera.partyinfo.node.Recipient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

public class DiscoveryTest {

    private Discovery discovery;

    private Enclave enclave;

    private NetworkStore networkStore;

    private RuntimeContext runtimeContext;

    @Before
    public void onSetUp() {

        enclave = mock(Enclave.class);
        networkStore = NetworkStore.getInstance();
        discovery = new Discovery() {
            @Override
            public void onUpdate(NodeInfo nodeInfo) {
            }

            @Override
            public void onDisconnect(URI nodeUri) {
            }


        };

        runtimeContext = RuntimeContext.getInstance();
    }

    @After
    public void onTearDown() {
        networkStore.getActiveNodes()
            .map(ActiveNode::getUri).forEach(networkStore::remove);

        verifyNoMoreInteractions(enclave,runtimeContext);
        MockContextHolder.reset();
        MockOnCreateHelper.reset();
    }

    @Test
    public void getCurrentWithUriOnly() {

        final URI uri = URI.create("http://somedomain.com");
        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        NodeInfo result = discovery.getCurrent();
        assertThat(result).isNotNull();
        verify(runtimeContext).getP2pServerUri();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
        assertThat(result.getParties()).isEmpty();
        assertThat(result.getRecipients()).isEmpty();

    }
    @Test
    public void getCurrent() {

        final URI uri = URI.create("http://somedomain.com");
        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        final List<PublicKey> keys = IntStream.range(0,5)
            .mapToObj(i -> mock(PublicKey.class))
            .collect(Collectors.toList());

        final ActiveNode activeNode = ActiveNode.Builder.create()
            .withUri(NodeUri.create(uri))
            .withKeys(keys)
            .build();

        networkStore.store(activeNode);

        NodeInfo result = discovery.getCurrent();
        assertThat(result).isNotNull();
        verify(runtimeContext).getP2pServerUri();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
        assertThat(result.getParties()).hasSize(1);
        assertThat(result.getRecipients()).hasSize(5);

        List<Recipient> recipients = List.copyOf(result.getRecipients());
        assertThat(recipients.stream().map(Recipient::getKey)
            .collect(Collectors.toList()))
            .containsExactlyInAnyOrderElementsOf(keys);

    }

    @Test
    public void getCurrentWithNoKeys() {

        final URI uri = URI.create("http://somedomain.com");
        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        final List<PublicKey> keys = List.of();

        final ActiveNode activeNode = ActiveNode.Builder.create()
            .withUri(NodeUri.create(uri))
            .withKeys(keys)
            .build();

        networkStore.store(activeNode);

        NodeInfo result = discovery.getCurrent();
        assertThat(result).isNotNull();
        verify(runtimeContext).getP2pServerUri();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");

        assertThat(result.getParties()).hasSize(1);
        assertThat(result.getRecipients()).isEmpty();
        assertThat(result.getParties()).containsExactly(new Party("http://somedomain.com/"));
    }

    @Test
    public void getInstance() {
        Discovery instance = Discovery.getInstance();
        assertThat(instance).isExactlyInstanceOf(DiscoveryFactory.class);
        verify(runtimeContext).isDisablePeerDiscovery();
    }

    @Test
    public void onCreate() {
        MockOnCreateHelper onCreateHelper = MockOnCreateHelper.class.cast(OnCreateHelper.getInstance());
        discovery.onCreate();
        assertThat(onCreateHelper.getInvocationCount()).isEqualTo(1);
        discovery.onCreate();
        assertThat(onCreateHelper.getInvocationCount()).isEqualTo(2);


    }
}
