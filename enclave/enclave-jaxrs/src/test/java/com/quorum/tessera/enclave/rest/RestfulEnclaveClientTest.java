package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.KeyConfiguration;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.service.Service;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class RestfulEnclaveClientTest {

    private Enclave enclave;

    private JerseyTest jersey;

    private RestfulEnclaveClient enclaveClient;


    @Before
    public void setUp() throws Exception {
        enclave = mock(Enclave.class);
        when(enclave.status()).thenReturn(Service.Status.STARTED);
        
        jersey = Util.create(enclave);

        jersey.setUp();

        Config config = new Config();
        
        config.setAlwaysSendTo(java.util.Arrays.asList("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc="));
        
        config.setKeys(new KeyConfiguration());
        ConfigKeyPair keyPair = mock(ConfigKeyPair.class);
        when(keyPair.getPublicKey()).thenReturn("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");

        config.getKeys().setKeyData(Arrays.asList(keyPair));
        
        enclaveClient = new RestfulEnclaveClient(jersey.client(), jersey.target().getUri(),config);

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(enclave);
        jersey.tearDown();
    }

    @Test
    public void defaultPublicKey() {

        PublicKey result = enclaveClient.defaultPublicKey();
        assertThat(result).isNotNull();
        assertThat(result.encodeToBase64()).isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");


    }

    @Test
    public void getPublicKeys() {

        Set<PublicKey> result = enclaveClient.getPublicKeys();

        assertThat(result).hasSize(1);
        
         assertThat(result.iterator().next().encodeToBase64())
                 .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");


    }

    @Test
    public void getForwardingKeys() {


        Set<PublicKey> result = enclaveClient.getForwardingKeys();

        assertThat(result).hasSize(1);
        
         assertThat(result.iterator().next().encodeToBase64())
                 .isEqualTo("/+UuD63zItL1EbjxkKUljMgG8Z1w0AJ8pNOR4iq2yQc=");

    }

    @Test
    public void encryptPayload() {

        byte[] message = "HELLOW".getBytes();

        PublicKey senderPublicKey = PublicKey.from("PublicKey".getBytes());
        List<PublicKey> recipientPublicKeys = Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

        EncodedPayload encodedPayload = Fixtures.createSample();

        when(enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys))
                .thenReturn(encodedPayload);

        EncodedPayload result = enclaveClient.encryptPayload(message, senderPublicKey, recipientPublicKeys);

        assertThat(result).isNotNull();

        byte[] encodedResult = PayloadEncoder.create().encode(result);
        byte[] encodedEncodedPayload = PayloadEncoder.create().encode(encodedPayload);

        assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

        verify(enclave).encryptPayload(message, senderPublicKey, recipientPublicKeys);
        verify(enclave).status();

    }

    @Test
    public void encryptPayloadRaw() {

        byte[] message = "HELLOW".getBytes();

        byte[] encryptedKey = "encryptedKey".getBytes();

        Nonce nonce = new Nonce("Nonce".getBytes());

        PublicKey senderPublicKey = PublicKey.from("SenderPublicKey".getBytes());

        List<PublicKey> recipientPublicKeys = Arrays.asList(PublicKey.from("RecipientPublicKey".getBytes()));

        RawTransaction rawTransaction = new RawTransaction(message, encryptedKey, nonce, senderPublicKey);

        EncodedPayload encodedPayload = Fixtures.createSample();

        when(enclave.encryptPayload(any(RawTransaction.class), any(List.class)))
                .thenReturn(encodedPayload);

        EncodedPayload result = enclaveClient.encryptPayload(rawTransaction, recipientPublicKeys);

        assertThat(result).isNotNull();

        byte[] encodedResult = PayloadEncoder.create().encode(result);
        byte[] encodedEncodedPayload = PayloadEncoder.create().encode(encodedPayload);

        assertThat(encodedResult).isEqualTo(encodedEncodedPayload);

        verify(enclave).encryptPayload(any(RawTransaction.class), any(List.class));
        verify(enclave).status();
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
        verify(enclave).status();
    }

    @Test
    public void unencryptTransaction() throws Exception {

        EncodedPayload payload = Fixtures.createSample();

        PublicKey providedKey = PublicKey.from("ProvidedKey".getBytes());

        byte[] outcome = "SUCCESS".getBytes();

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(outcome);

        byte[] result = enclaveClient.unencryptTransaction(payload, providedKey);

        assertThat(result).isEqualTo(outcome);

        verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).status();
    }

    @Test
    public void createNewRecipientBox() {

        EncodedPayload payload = Fixtures.createSample();

        PublicKey providedKey = PublicKey.from("ProvidedKey".getBytes());

        byte[] outcome = "SUCCESS".getBytes();

        when(enclave.createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class))).thenReturn(outcome);

        byte[] result = enclaveClient.createNewRecipientBox(payload, providedKey);

        assertThat(result).isEqualTo(outcome);

        verify(enclave).createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class));
        verify(enclave).status();
    }

    @Test
    public void statusStarted() {
        when(enclave.status())
                .thenReturn(Service.Status.STARTED);
        assertThat(enclaveClient.status())
                .isEqualTo(Service.Status.STARTED);

        verify(enclave).status();
    }

    @Test
    public void statusStopped() {
        when(enclave.status())
                .thenThrow(RuntimeException.class);
        assertThat(enclaveClient.status())
                .isEqualTo(Service.Status.STOPPED);
        verify(enclave).status();
    }
}
