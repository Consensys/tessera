package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
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
    
    @Test
    public void addRecipientToPayload() {

        PublicKey senderKey = mock(PublicKey.class);
        byte[] cipherText = "CIPHER_TEXT".getBytes();
        Nonce cipherTextNonce = mock(Nonce.class);
        Nonce recipientNonce = mock(Nonce.class);
        
        List<byte[]> recipientBoxes = Arrays.asList("RBOX".getBytes());
        EncodedPayload encodedPayload = new EncodedPayload(senderKey,cipherText,cipherTextNonce,recipientBoxes,recipientNonce);
 
        byte[] keyBytes = "".getBytes();
        PublicKey recipientKey = PublicKey.from(keyBytes); 
        List<PublicKey> recipientKeys = Arrays.asList(recipientKey);
        
        EncodedPayloadWithRecipients encodedPayloadWithRecipients = 
                new EncodedPayloadWithRecipients(encodedPayload, recipientKeys);
        

        EncodedPayloadWithRecipients result = enclave.extractRecipientBoxForRecipientAndAddToNestedPayload(encodedPayloadWithRecipients, recipientKey);
        
        assertThat(result.getRecipientKeys()).isEmpty();
    }

}
