package com.quorum.tessera.discovery;

import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.MockContextHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

public class OnCreateHelperTest {

    private Enclave enclave;

    private NetworkStore networkStore;

    private OnCreateHelperImpl onCreateHelper;

    private RuntimeContext runtimeContext;

    @Before
    public void onSetup() {
        this.runtimeContext = RuntimeContext.getInstance();
        this.enclave = mock(Enclave.class);
        this.networkStore = mock(NetworkStore.class);
        this.onCreateHelper = new OnCreateHelperImpl(enclave,networkStore);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(enclave,networkStore,runtimeContext);
        MockContextHolder.reset();
    }

    @Test
    public void onCreate() {
        URI uri = URI.create("http://somedomain.com/");
        when(runtimeContext.getPeers()).thenReturn(List.of(uri));

        when(runtimeContext.getP2pServerUri()).thenReturn(uri);

        PublicKey publicKey = mock(PublicKey.class);
        when(enclave.getPublicKeys()).thenReturn(Set.of(publicKey));

        onCreateHelper.onCreate();


        verify(networkStore,times(2)).store(any(ActiveNode.class));
        verify(runtimeContext).getPeers();
        verify(runtimeContext).getP2pServerUri();
        verify(enclave).getPublicKeys();

    }

}
