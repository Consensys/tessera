package com.quorum.tessera.api.common;

import com.jpmorgan.quorum.mock.servicelocator.MockServiceLocator;
import com.quorum.tessera.api.StoreRawRequest;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.locator.ServiceLocator;
import com.quorum.tessera.transaction.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RawTransactionResourceTest {

    private RawTransactionResource transactionResource;

    private TransactionManager transactionManager;

    @Before
    public void onSetup() {
        this.transactionManager = mock(TransactionManager.class);

        this.transactionResource = new RawTransactionResource(transactionManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void store() {
        invokeStoreAndCheck(transactionResource, transactionManager);
    }

    @Test
    public void testNoParamConstructor() {
        MockServiceLocator serviceLocator = (MockServiceLocator) ServiceLocator.create();
        Set services = new HashSet();
        TransactionManager tm = mock(TransactionManager.class);
        when(tm.defaultPublicKey()).thenReturn(mock(PublicKey.class));
        services.add(tm);
        serviceLocator.setServices(services);

        RawTransactionResource tr = new RawTransactionResource();

        invokeStoreAndCheck(tr, tm);
    }

    private void invokeStoreAndCheck(RawTransactionResource rawTransactionResource, TransactionManager tm) {
        final StoreRawRequest storeRawRequest = new StoreRawRequest();
        storeRawRequest.setPayload("PAYLOAD".getBytes());
        storeRawRequest.setFrom("Sender".getBytes());
        final Response result = rawTransactionResource.store(storeRawRequest);

        assertThat(result.getStatus()).isEqualTo(200);
        verify(tm).store(any());
        verify(tm).defaultPublicKey();
    }
}
