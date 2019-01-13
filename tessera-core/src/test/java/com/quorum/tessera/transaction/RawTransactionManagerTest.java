package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.StoreRawRequest;
import com.quorum.tessera.api.model.StoreRawResponse;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.enclave.model.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.RawTransaction;
import com.quorum.tessera.nacl.Nonce;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RawTransactionManagerTest {

    private EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private Enclave enclave;

    private RawTransactionManager rawTransactionManager;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    @Before
    public void onSetUp() {
        enclave = mock(Enclave.class);
        encryptedRawTransactionDAO = mock(EncryptedRawTransactionDAO.class);
        rawTransactionManager = new RawTransactionManagerImpl(enclave, encryptedRawTransactionDAO);
    }

    @After
    public void onTearDown() {
        verifyNoMoreInteractions(encryptedRawTransactionDAO, enclave);
    }

    @Test
    public void store() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction = new RawTransaction("CIPHERTEXT".getBytes(), "SomeKey".getBytes(),
            new Nonce("nonce".getBytes()),PublicKey.from(sender));
        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = new StoreRawRequest();
        sendRequest.setFrom(sender);
        sendRequest.setPayload(payload);
        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        StoreRawResponse result = rawTransactionManager.store(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).containsExactly(expectedHash.getHashBytes());

        verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
        verify(encryptedRawTransactionDAO).save(argThat(et -> {
            assertThat(et.getEncryptedKey()).containsExactly("SomeKey".getBytes());
            assertThat(et.getEncryptedPayload()).containsExactly("CIPHERTEXT".getBytes());
            assertThat(et.getHash()).isEqualTo(expectedHash);
            assertThat(et.getNonce()).containsExactly("nonce".getBytes());
            assertThat(et.getSender()).containsExactly(sender);
            return true;
        }));
    }

    @Test
    public void storeWithEmptySender() {
        byte[] sender = "SENDER".getBytes();
        RawTransaction rawTransaction = new RawTransaction("CIPHERTEXT".getBytes(), "SomeKey".getBytes(),
            new Nonce("nonce".getBytes()),PublicKey.from(sender));
        when(enclave.encryptRawPayload(any(), any())).thenReturn(rawTransaction);
        when(enclave.defaultPublicKey()).thenReturn(PublicKey.from(sender));
        byte[] payload = Base64.getEncoder().encode("PAYLOAD".getBytes());
        StoreRawRequest sendRequest = new StoreRawRequest();
        sendRequest.setPayload(payload);
        MessageHash expectedHash = messageHashFactory.createFromCipherText("CIPHERTEXT".getBytes());

        StoreRawResponse result = rawTransactionManager.store(sendRequest);

        assertThat(result).isNotNull();
        assertThat(result.getKey()).containsExactly(expectedHash.getHashBytes());

        verify(enclave).encryptRawPayload(eq(payload), eq(PublicKey.from(sender)));
        verify(enclave).defaultPublicKey();
        verify(encryptedRawTransactionDAO).save(argThat(et -> {
            assertThat(et.getEncryptedKey()).containsExactly("SomeKey".getBytes());
            assertThat(et.getEncryptedPayload()).containsExactly("CIPHERTEXT".getBytes());
            assertThat(et.getHash()).isEqualTo(expectedHash);
            assertThat(et.getNonce()).containsExactly("nonce".getBytes());
            assertThat(et.getSender()).containsExactly(sender);
            return true;
        }));
    }
}
