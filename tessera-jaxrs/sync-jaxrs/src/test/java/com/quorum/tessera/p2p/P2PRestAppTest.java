package com.quorum.tessera.p2p;

import com.quorum.tessera.api.filter.IPWhitelistFilter;
import com.quorum.tessera.config.AppType;
import com.quorum.tessera.context.RuntimeContext;
import com.quorum.tessera.discovery.Discovery;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.p2p.partyinfo.PartyStore;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mockStatic;

public class P2PRestAppTest {

    private RuntimeContext runtimeContext;

    private P2PRestApp p2PRestApp;

    private Enclave enclave;

    private Discovery discovery;

    private PartyStore partyStore;

    private TransactionManager transactionManager;

    private PayloadEncoder payloadEncoder;

    private BatchResendManager batchResendManager;

    private URI peerUri = URI.create("junit");

    @Before
    public void setUp() throws Exception {

        runtimeContext = mock(RuntimeContext.class);

        enclave = mock(Enclave.class);
        discovery = mock(Discovery.class);
        partyStore = mock(PartyStore.class);
        transactionManager = mock(TransactionManager.class);
        batchResendManager = mock(BatchResendManager.class);
        payloadEncoder = PayloadEncoder.create();

        p2PRestApp = new P2PRestApp(discovery,enclave,partyStore,transactionManager,batchResendManager,payloadEncoder);

        Client client = mock(Client.class);
        when(runtimeContext.getP2pClient()).thenReturn(client);
        when(runtimeContext.isRemoteKeyValidation()).thenReturn(true);

        when(runtimeContext.getPeers())
            .thenReturn(List.of(peerUri));

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(runtimeContext);
        verifyNoMoreInteractions(enclave);
        verifyNoMoreInteractions(discovery);
        verifyNoMoreInteractions(partyStore);
        verifyNoMoreInteractions(transactionManager);
        verifyNoMoreInteractions(batchResendManager);
    }

    @Test
    public void getSingletons() {

        try(var mockedStaticRuntimeContext = mockStatic(RuntimeContext.class);
        ) {
            mockedStaticRuntimeContext.when(RuntimeContext::getInstance)
                .thenReturn(runtimeContext);

            Set<Object> results = p2PRestApp.getSingletons();
            assertThat(results).hasSize(3);
            results.forEach(
                o ->
                    assertThat(o)
                        .isInstanceOfAny(
                            PartyInfoResource.class, IPWhitelistFilter.class, TransactionResource.class));

            mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
            mockedStaticRuntimeContext.verifyNoMoreInteractions();
        }

        verify(runtimeContext).isRecoveryMode();
        verify(runtimeContext).getPeers();
        verify(runtimeContext).getP2pClient();
        verify(runtimeContext).isRemoteKeyValidation();
        verify(partyStore).store(peerUri);
    }

    @Test
    public void getSingletonsRecoverP2PApp() {

        when(runtimeContext.isRecoveryMode()).thenReturn(true);

        try(var mockedStaticRuntimeContext = mockStatic(RuntimeContext.class);
        ) {
            mockedStaticRuntimeContext.when(RuntimeContext::getInstance).thenReturn(runtimeContext);

            Set<Object> results = p2PRestApp.getSingletons();
            assertThat(results).hasSize(3);
            results.forEach(
                o ->
                    assertThat(o)
                        .isInstanceOfAny(
                            PartyInfoResource.class, IPWhitelistFilter.class, RecoveryResource.class));

            mockedStaticRuntimeContext.verify(RuntimeContext::getInstance);
            mockedStaticRuntimeContext.verifyNoMoreInteractions();
        }

        verify(runtimeContext).isRecoveryMode();
        verify(runtimeContext).getPeers();
        verify(runtimeContext).getP2pClient();
        verify(runtimeContext).isRemoteKeyValidation();
        verify(partyStore).store(peerUri);
    }



    @Test
    public void appType() {
        assertThat(p2PRestApp.getAppType()).isEqualTo(AppType.P2P);
    }

    @Test
    public void defaultConstructor() {

        try(var e = mockStatic(Enclave.class);
            var d = mockStatic(Discovery.class);
            var p = mockStatic(PartyStore.class);
            var t = mockStatic(TransactionManager.class);
            var pe = mockStatic(PayloadEncoder.class);
            var b = mockStatic(BatchResendManager.class)
        ) {
            e.when(Enclave::create).thenReturn(enclave);
            d.when(Discovery::create).thenReturn(discovery);
            p.when(PartyStore::getInstance).thenReturn(partyStore);
            t.when(TransactionManager::create).thenReturn(transactionManager);
            pe.when(PayloadEncoder::create).thenReturn(mock(PayloadEncoder.class));
            b.when(BatchResendManager::create).thenReturn(batchResendManager);

            new P2PRestApp();

            e.verify(Enclave::create);
            e.verifyNoMoreInteractions();

            d.verify(Discovery::create);
            d.verifyNoMoreInteractions();

            p.verify(PartyStore::getInstance);
            p.verifyNoMoreInteractions();

            t.verify(TransactionManager::create);
            t.verifyNoMoreInteractions();

            pe.verify(PayloadEncoder::create);
            pe.verifyNoMoreInteractions();

            b.verify(BatchResendManager::create);
            b.verifyNoMoreInteractions();
        }

    }
}
