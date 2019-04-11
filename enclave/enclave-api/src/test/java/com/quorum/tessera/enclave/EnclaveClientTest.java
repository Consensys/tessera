package com.quorum.tessera.enclave;

import com.quorum.tessera.service.Service;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

public class EnclaveClientTest {

    private EnclaveClient enclaveClient;

    @Before
    public void onSetUp() {
        this.enclaveClient = mock(EnclaveClient.class); //TODO: not have this as a mock

        doCallRealMethod().when(enclaveClient).validateEnclaveStatus();
    }

    @After
    public void onTearDown() {
        verify(enclaveClient).validateEnclaveStatus();
        verifyNoMoreInteractions(enclaveClient);
    }

    @Test
    public void enclaveIsUp() {
        when(enclaveClient.status()).thenReturn(Service.Status.STARTED);

        enclaveClient.validateEnclaveStatus();

        verify(enclaveClient).status();
    }

    @Test
    public void enclaveIsDown() {
        when(enclaveClient.status()).thenReturn(Service.Status.STOPPED);

        final Throwable throwable = catchThrowable(enclaveClient::validateEnclaveStatus);

        assertThat(throwable).isInstanceOf(EnclaveNotAvailableException.class);

        verify(enclaveClient).status();
    }

}
