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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DiscoveryHelperTest {

    private Enclave enclave;

    private NetworkStore networkStore;

    private RuntimeContext runtimeContext;

    private DiscoveryHelper discoveryHelper;

    @Before
    public void beforeTest() {
        this.runtimeContext = RuntimeContext.getInstance();
        this.enclave = mock(Enclave.class);
        this.networkStore = mock(NetworkStore.class);
        this.discoveryHelper = new DiscoveryHelperImpl(networkStore,enclave);
    }

    @After
    public void afterTest() {
        verifyNoMoreInteractions(enclave,networkStore,runtimeContext);
        MockContextHolder.reset();
        MockDiscoveryHelper.reset();
    }

    @Test
    public void onCreate() {
        URI uri = URI.create("http://somedomain.com/");
        when(runtimeContext.getPeers()).thenReturn(List.of(uri));

        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        discoveryHelper.onCreate();


        verify(networkStore,times(2)).store(any(ActiveNode.class));
        verify(runtimeContext).getPeers();
        verify(runtimeContext).getP2pServerUri();
        verify(enclave).getPublicKeys();

    }

    @Test
    public void buildCurrent() {

        final URI uri = URI.create("http://somedomain.com");
        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        final List<PublicKey> keys = IntStream.range(0,5)
            .mapToObj(i -> mock(PublicKey.class))
            .collect(Collectors.toList());

        final ActiveNode activeNode = ActiveNode.Builder.create()
            .withUri(NodeUri.create(uri))
            .withKeys(keys)
            .build();

        when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

        NodeInfo result = discoveryHelper.buildCurrent();
        assertThat(result).isNotNull();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
        assertThat(result.getParties()).hasSize(1);
        assertThat(result.getRecipients()).hasSize(5);

        List<Recipient> recipients = List.copyOf(result.getRecipients());
        assertThat(recipients.stream().map(Recipient::getKey)
            .collect(Collectors.toList()))
            .containsExactlyInAnyOrderElementsOf(keys);

        verify(networkStore).getActiveNodes();
        verify(runtimeContext).getP2pServerUri();


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

        when(networkStore.getActiveNodes()).thenReturn(Stream.of(activeNode));

        NodeInfo result = discoveryHelper.buildCurrent();
        assertThat(result).isNotNull();
        verify(runtimeContext).getP2pServerUri();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
        verify(networkStore).getActiveNodes();
        assertThat(result.getParties()).hasSize(1);
        assertThat(result.getRecipients()).isEmpty();
        assertThat(result.getParties()).containsExactly(new Party("http://somedomain.com/"));
    }

    @Test
    public void getCurrentWithUriOnly() {

        final URI uri = URI.create("http://somedomain.com");
        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        NodeInfo result = discoveryHelper.buildCurrent();
        assertThat(result).isNotNull();
        verify(runtimeContext).getP2pServerUri();

        assertThat(result.getUrl()).isEqualTo("http://somedomain.com/");
        assertThat(result.getParties()).isEmpty();
        assertThat(result.getRecipients()).isEmpty();
        verify(networkStore).getActiveNodes();

    }

}
