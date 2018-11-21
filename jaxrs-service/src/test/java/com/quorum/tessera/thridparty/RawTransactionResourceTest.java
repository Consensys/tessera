package com.quorum.tessera.thridparty;

import com.quorum.tessera.api.model.StoreRawRequest;
import com.quorum.tessera.transaction.RawTransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

public class RawTransactionResourceTest {

    private RawTransactionResource transactionResource;

    private RawTransactionManager transactionManager;

    @Before
    public void onSetup() {
        transactionManager = mock(RawTransactionManager.class);
        transactionResource = new RawTransactionResource(transactionManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(transactionManager);
    }

    @Test
    public void store() {
        StoreRawRequest storeRawRequest = new StoreRawRequest();
        storeRawRequest.setPayload("PAYLOAD".getBytes());

        Response result = transactionResource.store(storeRawRequest);
        assertThat(result.getStatus()).isEqualTo(200);

        verify(transactionManager).store(same(storeRawRequest));
    }
}
