package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.NaclFacade;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class EnclaveTest {

    private Enclave enclave;

    private NaclFacade nacl;

    private KeyManager keyManager;

    @Before
    public void onSetUp() {
        nacl = mock(NaclFacade.class);
        keyManager = mock(KeyManager.class);
        enclave = new EnclaveImpl(nacl, keyManager);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(nacl, keyManager);
    }

    @Test
    public void defaultPublicKey() {
        enclave.defaultPublicKey();
        verify(keyManager).defaultPublicKey();
    }

    @Test
    public void getForwardingKeys() {
        enclave.getForwardingKeys();
        verify(keyManager).getForwardingKeys();
        
    }
    
     @Test
    public void getPublicKeys() {
        enclave.getPublicKeys();
        verify(keyManager).getPublicKeys();
    }

}
