package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.encryption.RawTransaction;
import com.quorum.tessera.nacl.Nonce;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class EnclaveClientTest {

    private Enclave enclave;

    private JerseyTest jersey;

    private EnclaveClient enclaveClient;

    @Before
    public void setUp() throws Exception {
        enclave = mock(Enclave.class);

        jersey = Util.create(enclave);

        jersey.setUp();

        enclaveClient = new EnclaveClient(jersey.client(),jersey.target().getUri());

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(enclave);
        jersey.tearDown();
    }

    @Test
    public void defaultPublicKey() {

        PublicKey key = PublicKey.from("HELLOW".getBytes());

        when(enclave.defaultPublicKey()).thenReturn(key);

        PublicKey result = enclaveClient.defaultPublicKey();

        assertThat(result).isEqualTo(key);

        verify(enclave).defaultPublicKey();

    }

    @Test
    public void getPublicKeys() {

        PublicKey key = PublicKey.from("HELLOW".getBytes());

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(key));

        Set<PublicKey> result = enclaveClient.getPublicKeys();

        assertThat(result).containsExactly(key);

        verify(enclave).getPublicKeys();

    }

    @Test
    public void getForwardingKeys() {

        PublicKey key = PublicKey.from("HELLOW".getBytes());

        when(enclave.getForwardingKeys()).thenReturn(Collections.singleton(key));

        Set<PublicKey> result = enclaveClient.getForwardingKeys();

        assertThat(result).containsExactly(key);

        verify(enclave).getForwardingKeys();

    }

    @Test
    public void encryptPayload() {

        byte[] message = "HELLOW".getBytes();

        PublicKey senderPublicKey = PublicKey.from("PublicKey".getBytes());
        List<PublicKey> recipientPublicKeys = Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

        EncodedPayloadWithRecipients encodedPayload = Fixtures.createSample();

        when(enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys))
                .thenReturn(encodedPayload);

        EncodedPayloadWithRecipients result = enclaveClient.encryptPayload(message, senderPublicKey, recipientPublicKeys);

        assertThat(result).isNotNull();

        byte[] encodedResult = PayloadEncoder.create().encode(result);
        byte[] encodedEncodedPayload = PayloadEncoder.create().encode(encodedPayload);

        assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

        verify(enclave).encryptPayload(message, senderPublicKey, recipientPublicKeys);

    }

    @Test
    public void encryptPayloadRaw() {

        byte[] message = "HELLOW".getBytes();

        byte[] encryptedKey = "encryptedKey".getBytes();

        Nonce nonce = new Nonce("Nonce".getBytes());

        PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

        List<PublicKey> recipientPublicKeys = Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

        RawTransaction rawTransaction = new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

        EncodedPayloadWithRecipients encodedPayload = Fixtures.createSample();

        when(enclave.encryptPayload(any(RawTransaction.class), any(List.class)))
                .thenReturn(encodedPayload);

        EncodedPayloadWithRecipients result = enclaveClient.encryptPayload(rawTransaction, recipientPublicKeys);

        assertThat(result).isNotNull();

        byte[] encodedResult = PayloadEncoder.create().encode(result);
        byte[] encodedEncodedPayload = PayloadEncoder.create().encode(encodedPayload);

        assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

        verify(enclave).encryptPayload(any(RawTransaction.class), any(List.class));

    }

    @Test
    public void encryptPayloadToRaw() {

        byte[] message = "HELLOW".getBytes();

        PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

        byte[] encryptedKey = "encryptedKey".getBytes();
        Nonce nonce = new Nonce("Nonce".getBytes());
        RawTransaction rawTransaction = new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

        when(enclave.encryptRawPayload(message, senderPublicKey)).thenReturn(rawTransaction);

        RawTransaction result = enclaveClient.encryptRawPayload(message, senderPublicKey);

        assertThat(result).isNotNull();

        assertThat(result).isEqualTo(rawTransaction);

        verify(enclave).encryptRawPayload(message, senderPublicKey);

    }


    @Test
    public void unencryptTransaction() throws Exception {
        
        EncodedPayloadWithRecipients payloadWithRecipients = Fixtures.createSample();
        
        PublicKey providedKey = PublicKey.from("ProvidedKey".getBytes());

        byte[] outcome = "SUCCESS".getBytes();
        
        when(enclave.unencryptTransaction(any(EncodedPayloadWithRecipients.class),any(PublicKey.class)))
                .thenReturn(outcome);
        
        byte[] result = enclaveClient.unencryptTransaction(payloadWithRecipients, providedKey);
        
        assertThat(result).isEqualTo(outcome);
      
        verify(enclave)
                .unencryptTransaction(any(EncodedPayloadWithRecipients.class),any(PublicKey.class));
        
    }

}
