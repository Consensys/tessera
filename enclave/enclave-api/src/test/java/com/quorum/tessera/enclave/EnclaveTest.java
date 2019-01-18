package com.quorum.tessera.enclave;

import com.quorum.tessera.encryption.*;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class EnclaveTest {

    private Enclave enclave;

    private NaclFacade nacl;

    private KeyManager keyManager;

    @Before
    public void onSetUp() {
        this.nacl = mock(NaclFacade.class);
        this.keyManager = mock(KeyManager.class);

        this.enclave = new EnclaveImpl(nacl, keyManager);
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

        EncodedPayload payload = new EncodedPayload(
            senderKey, cipherText, cipherTextNonce,
            singletonList(recipientBox), recipientNonce, singletonList(recipientKey)
        );

        when(keyManager.getPublicKeys()).thenReturn(Collections.singleton(senderKey));

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(senderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class))).thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payload, providedSenderKey);

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

        EncodedPayload payload = new EncodedPayload(
            senderKey, cipherText, cipherTextNonce,
            singletonList(recipientBox), recipientNonce, singletonList(recipientKey)
        );

        when(keyManager.getPublicKeys()).thenReturn(Collections.emptySet());

        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        when(keyManager.getPrivateKeyForPublicKey(providedSenderKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] expectedOutcome = "SUCCESS".getBytes();

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(SharedKey.class)))
                .thenReturn("sharedOrMasterKeyBytes".getBytes());

        when(nacl.openAfterPrecomputation(any(byte[].class), any(Nonce.class), any(MasterKey.class))).thenReturn(expectedOutcome);

        byte[] result = enclave.unencryptTransaction(payload, providedSenderKey);

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

        EncodedPayload result = enclave.encryptPayload(message, senderPublicKey, Arrays.asList(recipientPublicKey));

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getCipherText()).isEqualTo(cipherText);
        assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getRecipientBoxes()).containsExactly(encryptedMasterKeys);

        verify(nacl).createMasterKey();
        verify(nacl, times(2)).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
    }

    @Test
    public void encryptPayloadRawTransaction() {

        byte[] message = "MESSAGE".getBytes();

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        PublicKey senderPublicKey = PublicKey.from("SENDER".getBytes());
        PrivateKey senderPrivateKey = mock(PrivateKey.class);

        PublicKey recipientPublicKey = PublicKey.from("RECIPIENT".getBytes());
        Nonce cipherNonce = new Nonce("NONCE".getBytes());
        byte[] cipherText = "cipherText".getBytes();
        byte[] encryptedKeyBytes = "ENCRYPTED_KEY".getBytes();

        RawTransaction rawTransaction = new RawTransaction(cipherText, encryptedKeyBytes, cipherNonce, senderPublicKey);

        Nonce recipientNonce = mock(Nonce.class);

        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKeyForSender = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderPublicKey, senderPrivateKey)).thenReturn(sharedKeyForSender);

        when(nacl.openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender)).thenReturn(masterKeyBytes);

        when(nacl.randomNonce()).thenReturn(recipientNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(recipientPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKeys = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey)).thenReturn(encryptedMasterKeys);

        EncodedPayload result = enclave.encryptPayload(rawTransaction, Arrays.asList(recipientPublicKey));

        assertThat(result).isNotNull();
        assertThat(result.getRecipientKeys()).containsExactly(recipientPublicKey);
        assertThat(result.getCipherText()).isEqualTo(cipherText);
        assertThat(result.getCipherTextNonce()).isEqualTo(cipherNonce);
        assertThat(result.getSenderKey()).isEqualTo(senderPublicKey);
        assertThat(result.getRecipientBoxes()).containsExactly(encryptedMasterKeys);

        verify(nacl).randomNonce();
        verify(nacl).openAfterPrecomputation(encryptedKeyBytes, cipherNonce, sharedKeyForSender);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, recipientNonce, sharedKey);
        verify(nacl).computeSharedKey(recipientPublicKey, senderPrivateKey);
        verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
        verify(keyManager, times(2)).getPrivateKeyForPublicKey(senderPublicKey);
    }

    @Test
    public void encryptRawPayload() {

        byte[] message = "MESSAGE".getBytes();

        PublicKey senderPublicKey = mock(PublicKey.class);

        byte[] masterKeyBytes = "masterKeyBytes".getBytes();
        MasterKey masterKey = MasterKey.from(masterKeyBytes);
        Nonce cipherNonce = mock(Nonce.class);

        byte[] cipherText = "cipherText".getBytes();

        when(nacl.createMasterKey()).thenReturn(masterKey);
        when(nacl.randomNonce()).thenReturn(cipherNonce);

        when(nacl.sealAfterPrecomputation(message, cipherNonce, masterKey)).thenReturn(cipherText);

        PrivateKey senderPrivateKey = mock(PrivateKey.class);
        when(keyManager.getPrivateKeyForPublicKey(senderPublicKey)).thenReturn(senderPrivateKey);

        SharedKey sharedKey = mock(SharedKey.class);
        when(nacl.computeSharedKey(senderPublicKey, senderPrivateKey)).thenReturn(sharedKey);

        byte[] encryptedMasterKey = "encryptedMasterKeys".getBytes();
        when(nacl.sealAfterPrecomputation(masterKeyBytes, cipherNonce, sharedKey)).thenReturn(encryptedMasterKey);

        RawTransaction result = enclave.encryptRawPayload(message, senderPublicKey);

        assertThat(result).isNotNull();
        assertThat(result.getFrom()).isEqualTo(senderPublicKey);
        assertThat(result.getEncryptedPayload()).isEqualTo(cipherText);
        assertThat(result.getNonce()).isEqualTo(cipherNonce);
        assertThat(result.getEncryptedKey()).isEqualTo(encryptedMasterKey);


        verify(nacl).createMasterKey();
        verify(nacl, times(1)).randomNonce();
        verify(nacl).sealAfterPrecomputation(message, cipherNonce, masterKey);
        verify(nacl).sealAfterPrecomputation(masterKeyBytes, cipherNonce, sharedKey);
        verify(nacl).computeSharedKey(senderPublicKey, senderPrivateKey);
        verify(keyManager).getPrivateKeyForPublicKey(senderPublicKey);
    }
}
