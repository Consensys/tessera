package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.Enclave;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.mockito.Mockito.*;

public class EnclaveKeySynchroniserTest {

    private static final String URL = "myurl.com/";

    private Enclave enclave;

    private PartyInfoService partyInfoService;

    private EnclaveKeySynchroniser enclaveKeySynchroniser;

    @Before
    public void init() throws URISyntaxException {

        this.partyInfoService = mock(PartyInfoService.class);

        this.enclaveKeySynchroniser = new EnclaveKeySynchroniser(partyInfoService);
    }

    @After
    public void after() {
        verifyNoMoreInteractions(partyInfoService);
    }


    @Test
    public void run() {
        enclaveKeySynchroniser.run();
        verify(partyInfoService).syncKeys();

    }

}
