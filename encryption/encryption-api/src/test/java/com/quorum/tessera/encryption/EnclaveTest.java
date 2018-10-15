package com.quorum.tessera.encryption;

import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;
import java.util.Collections;
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
    public void unencryptTransaction() {

        PublicKey senderKey = mock(PublicKey.class);

        PublicKey recipientKey = mock(PublicKey.class);

        PublicKey providedSenderKey = mock(PublicKey.class);

        byte[] cipherText = "cipherText".getBytes();

        Nonce cipherTextNonce = mock(Nonce.class);

        byte[] recipientBox = "RecipientBox".getBytes();

        Nonce recipientNonce = mock(Nonce.class);

        EncodedPayload encodedPayload
                = new EncodedPayload(senderKey, cipherText, cipherTextNonce, Arrays.asList(recipientBox), recipientNonce);

        EncodedPayloadWithRecipients payloadWithRecipients
                = new EncodedPayloadWithRecipients(encodedPayload, Arrays.asList(recipientKey));

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderKey));

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class))).thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payloadWithRecipients, providedSenderKey);

        assertThat(result).isNotNull().isSameAs(expectedOutcome);

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class));
        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class));
        verify(keyManager).getPrivateKeyForPublicKey(senderKey);
        verify(keyManager).getPublicKeys();
        verify(nacl).computeSharedKey(recipientKey, senderPrivateKey);
    }

    @Test
    public void unencryptTransactionFromAnotherNode() {

        PublicKey senderKey = mock(PublicKey.class);

        PublicKey recipientKey = mock(PublicKey.class);

        PublicKey providedSenderKey = mock(PublicKey.class);

        byte[] cipherText = "cipherText".getBytes();

        Nonce cipherTextNonce = mock(Nonce.class);

        byte[] recipientBox = "RecipientBox".getBytes();

        Nonce recipientNonce = mock(Nonce.class);

        EncodedPayload encodedPayload
                = new EncodedPayload(senderKey, cipherText, cipherTextNonce, Arrays.asList(recipientBox), recipientNonce);

        EncodedPayloadWithRecipients payloadWithRecipients
                = new EncodedPayloadWithRecipients(encodedPayload, Arrays.asList(recipientKey));

        when(keyManager.getPublicKeys()).thenReturn(Collections.emptySet());

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(providedSenderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class))).thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payloadWithRecipients, providedSenderKey);

        assertThat(result).isNotNull().isSameAs(expectedOutcome);

        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class));
        verify(nacl).openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class));
        verify(keyManager).getPrivateKeyForPublicKey(providedSenderKey);
        verify(keyManager).getPublicKeys();
        verify(nacl).computeSharedKey(senderKey, senderPrivateKey);
    }

    @Test
    public void encryptPayload() {

        byte[] message = "MESSAGE".getBytes();

        PublicKey senderPublicKey = mock(PublicKey.class);
        PublicKey recipientPublicKey = mock(PublicKey.class);

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        Nonce cipherNonce = mock(Nonce.class);
        Nonce recipientNonce = mock(Nonce.class);

        byte[] cipherText = "cipherText".getBytes();

        when(nacl.createMasterKey()).thenReturn(masterKey);
        when(nacl.randomNonce()).thenReturn(cipherNonce, recipientNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        PrivateKey senderPrivateKey = mock(PrivateKey.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey)).thenReturn(encryptedMasterKeys);

        EncodedPayloadWithRecipients result = enclave.encryptPayload(message, senderPublicKey, Arrays.asList(recipientPublicKey));

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getEncodedPayload().getCipherText()).isEqualTo(cipherText);
        assertThat(result.getEncodedPayload().getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getEncodedPayload().getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getEncodedPayload().getRecipientBoxes()).containsExactly(encryptedMasterKeys);

        verify(nacl).createMasterKey();
        verify(nacl, times(2)).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
    }

}
