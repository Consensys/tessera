package com.jpmorgan.quorum.enclave.websockets;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadBuilder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.enclave.RawTransactionBuilder;
import com.quorum.tessera.encryption.PublicKey;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;
import org.glassfish.tyrus.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveEndpointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveEndpointTest.class);

    private Server server;

    private EnclaveAdapter enclaveAdapter;

    @Before
    public void onSetUp() throws Exception {
        server = new Server("localhost", 8025, "/", null, EnclaveEndpoint.class);
        server.start();

        enclaveAdapter = new EnclaveAdapter(URI.create("ws://localhost:8025/enclave"));
        enclaveAdapter.onConstruct();
    }

    @After
    public void onTearDown() {
        enclaveAdapter.onDestroy();
        server.stop();
    }

    @Test
    public void defaultPublicKey() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(MockEnclaveFactory.ENCLAVE.defaultPublicKey()).thenReturn(publicKey);

        PublicKey result = enclaveAdapter.defaultPublicKey();

        assertThat(result).isEqualTo(publicKey);
    }

    @Test
    public void forwardingKeys() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(MockEnclaveFactory.ENCLAVE.getForwardingKeys()).thenReturn(Collections.singleton(publicKey));

        Set<PublicKey> results = enclaveAdapter.getForwardingKeys();

        assertThat(results).containsExactly(publicKey);
    }

    @Test
    public void publicKeys() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(MockEnclaveFactory.ENCLAVE.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        Set<PublicKey> results = enclaveAdapter.getPublicKeys();

        assertThat(results).containsExactly(publicKey);
    }

    @Test
    public void encryptPayload() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey senderPublicKey = PublicKey.from(Base64.getDecoder().decode(key));

        byte[] message = "SOME MESSAGE".getBytes();

        PublicKey recipientPublicKey = PublicKey.from(Base64.getDecoder().decode(key));

        List<PublicKey> recipientPublicKeys = new ArrayList<>();
        recipientPublicKeys.add(recipientPublicKey);

        EncodedPayload encodedPayload = EncodedPayloadBuilder.create()
                .withSenderKey(PublicKey.from("senderKey".getBytes()))
                .withCipherText("cipherText".getBytes())
                .withCipherTextNonce("cipherTextNonce".getBytes())
                .withRecipientBoxes(Arrays.asList("recipientBox".getBytes()))
                .withRecipientNonce("recipientNonce".getBytes())
                .withRecipientKeys(PublicKey.from("recipientKey".getBytes()))
                .build();

        when(MockEnclaveFactory.ENCLAVE.encryptPayload(message, senderPublicKey, recipientPublicKeys)).thenReturn(encodedPayload);

        EncodedPayload result = enclaveAdapter.encryptPayload(message, senderPublicKey, recipientPublicKeys);

        assertThat(result).isNotNull();

    }

    @Test
    public void encryptRawTxnPayload() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey senderPublicKey = PublicKey.from(Base64.getDecoder().decode(key));

        byte[] message = "SOME MESSAGE".getBytes();

        EncodedPayload encodedPayload = EncodedPayloadBuilder.create()
                .withSenderKey(PublicKey.from("senderKey".getBytes()))
                .withCipherText("cipherText".getBytes())
                .withCipherTextNonce("cipherTextNonce".getBytes())
                .withRecipientBoxes(Arrays.asList("recipientBox".getBytes()))
                .withRecipientNonce("recipientNonce".getBytes())
                .withRecipientKeys(PublicKey.from("recipientKey".getBytes()))
                .build();

        RawTransaction txn = RawTransactionBuilder.create()
                .withEncryptedPayload(message)
                .withFrom(senderPublicKey).withEncryptedKey("PP".getBytes()).withNonce("nonce".getBytes()).build();

        when(MockEnclaveFactory.ENCLAVE.encryptPayload(txn, Arrays.asList(senderPublicKey))).thenReturn(encodedPayload);

        EncodedPayload result = enclaveAdapter.encryptPayload(txn, Arrays.asList(senderPublicKey));

        assertThat(result).isNotNull();

    }

    @Test
    public void encryptRawPayload() throws Exception {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey senderPublicKey = PublicKey.from(Base64.getDecoder().decode(key));

        byte[] message = "SOME MESSAGE".getBytes();

        RawTransaction txn = RawTransactionBuilder.create()
                .withEncryptedPayload(message)
                .withFrom(senderPublicKey).withEncryptedKey("PP".getBytes()).withNonce("nonce".getBytes()).build();

        when(MockEnclaveFactory.ENCLAVE.encryptRawPayload(message, senderPublicKey)).thenReturn(txn);

        RawTransaction result = enclaveAdapter.encryptRawPayload(message, senderPublicKey);

        assertThat(result).isNotNull();

    }

    @Test
    public void unencryptTransaction() throws Exception {

        
        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey providedKey = PublicKey.from(Base64.getDecoder().decode(key));
        
        EncodedPayload encodedPayload = EncodedPayloadBuilder.create()
                .withSenderKey(PublicKey.from("senderKey".getBytes()))
                .withCipherText("cipherText".getBytes())
                .withCipherTextNonce("cipherTextNonce".getBytes())
                .withRecipientBoxes(Arrays.asList("recipientBox".getBytes()))
                .withRecipientNonce("recipientNonce".getBytes())
                .withRecipientKeys(PublicKey.from("recipientKey".getBytes()))
                .build();
        
        byte[] outcome = "OUTCOME".getBytes();
        
        when(MockEnclaveFactory.ENCLAVE.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(outcome);

        byte[] result = enclaveAdapter.unencryptTransaction(encodedPayload,providedKey);
        
        assertThat(result).isEqualTo(outcome);
    }
    
    @Test
    public void createBoxData() throws Exception {

        
        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey providedKey = PublicKey.from(Base64.getDecoder().decode(key));
        
        EncodedPayload encodedPayload = EncodedPayloadBuilder.create()
                .withSenderKey(PublicKey.from("senderKey".getBytes()))
                .withCipherText("cipherText".getBytes())
                .withCipherTextNonce("cipherTextNonce".getBytes())
                .withRecipientBoxes(Arrays.asList("recipientBox".getBytes()))
                .withRecipientNonce("recipientNonce".getBytes())
                .withRecipientKeys(PublicKey.from("recipientKey".getBytes()))
                .build();
        
        byte[] outcome = "OUTCOME".getBytes();
        
        when(MockEnclaveFactory.ENCLAVE.createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(outcome);

        byte[] result = enclaveAdapter.createNewRecipientBox(encodedPayload,providedKey);
        
        assertThat(result).isEqualTo(outcome);
    }

}
