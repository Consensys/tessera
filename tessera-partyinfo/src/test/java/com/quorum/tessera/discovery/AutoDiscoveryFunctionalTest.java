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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AutoDiscoveryFunctionalTest {
    private AutoDiscovery discovery;

    private NetworkStore networkStore;

    private Enclave enclave;

    private RuntimeContext runtimeContext;

    @Before
    public void onSetUp() {
        networkStore = NetworkStore.getInstance();
        enclave = mock(Enclave.class);
        discovery = new AutoDiscovery(networkStore,enclave);
        runtimeContext = RuntimeContext.getInstance();
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave,runtimeContext);
        MockContextHolder.reset();
    }
    @Test
    public void partiesArePropagatedAcrossFourNodeNetwork() {


        final NodeInfo nodeInfo = NodeInfo.Builder.create()
            .withUrl("http://nodeone.com")
            .withRecipients(List.of(Recipient.of(mock(PublicKey.class),"http://nodeone.com")))
            .withParties(List.of(new Party("http://nodetwo.com")))
            .build();

        final NodeInfo nodeInfo2 = NodeInfo.Builder.create()
            .withUrl("http://nodetwo.com")
            .withRecipients(List.of(Recipient.of(mock(PublicKey.class),"http://nodetwo.com")))
            .withParties(List.of(new Party("http://nodethree.com")))
            .build();

        final NodeInfo nodeInfo3 = NodeInfo.Builder.create()
            .withUrl("http://nodethree.com")
            .withRecipients(List.of(Recipient.of(mock(PublicKey.class),"http://nodethree.com")))
            .withParties(List.of(new Party("http://nodefour.com")))
            .build();

        final NodeInfo nodeInfo4 = NodeInfo.Builder.create()
            .withUrl("http://nodefour.com")
            .withRecipients(List.of(Recipient.of(mock(PublicKey.class),"http://nodefour.com")))
            .withParties(List.of(new Party("http://nodeone.com")))
            .build();


        final List<NodeInfo> infos = new ArrayList<NodeInfo>(List.of(nodeInfo,nodeInfo2,nodeInfo3,nodeInfo4));
        Collections.shuffle(infos);

        infos.forEach(discovery::onUpdate);


        final NodeInfo result = discovery.getCurrent();

        assertThat(result.getParties()).hasSize(4);
        assertThat(result.getRecipients()).hasSize(4);

        verify(runtimeContext,times(5)).getP2pServerUri();
    }

}
