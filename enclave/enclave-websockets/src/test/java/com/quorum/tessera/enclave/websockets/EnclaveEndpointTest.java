package com.quorum.tessera.enclave.websockets;

import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ServerConfig;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadBuilder;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.enclave.RawTransactionBuilder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.server.TesseraServer;
import com.quorum.tessera.server.TesseraServerFactory;
import com.quorum.tessera.service.Service;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.websocket.Session;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnclaveEndpointTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveEndpointTest.class);

    private TesseraServerFactory serverFactory = TesseraServerFactory.create(CommunicationType.WEB_SOCKET);
    
    private TesseraServer server;

    private WebsocketEnclaveClient enclaveAdapter;

    private Enclave enclave;
    
    @Before
    public void onSetUp() throws Exception {
        EnclaveFactory enclaveFactory = mock(EnclaveFactory.class);
        
        enclave = mock(Enclave.class);

        Config config = mock(Config.class);
        EnclaveHolder.instance(enclaveFactory,config);
        when(enclaveFactory.createLocal(config)).thenReturn(enclave);

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setBindingAddress("ws://localhost:8025");
        serverConfig.setServerAddress("ws://localhost:8025");
        server = serverFactory.createServer(serverConfig, Collections.singleton(EnclaveEndpoint.class));
        server.start();

        enclaveAdapter = new WebsocketEnclaveClient(URI.create("ws://localhost:8025/enclave"));
        enclaveAdapter.start();
    }

    @After
    public void onTearDown() throws Exception {
        enclaveAdapter.stop();
        server.stop();
    }

    @Test
    public void defaultPublicKey() {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(enclave.defaultPublicKey()).thenReturn(publicKey);

        PublicKey result = enclaveAdapter.defaultPublicKey();

        assertThat(result).isEqualTo(publicKey);
    }

    @Test
    public void forwardingKeys() {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(enclave.getForwardingKeys()).thenReturn(Collections.singleton(publicKey));

        Set<PublicKey> results = enclaveAdapter.getForwardingKeys();

        assertThat(results).containsExactly(publicKey);
    }

    @Test
    public void publicKeys() {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey publicKey = PublicKey.from(Base64.getDecoder().decode(key));

        when(enclave.getPublicKeys()).thenReturn(Collections.singleton(publicKey));

        Set<PublicKey> results = enclaveAdapter.getPublicKeys();

        assertThat(results).containsExactly(publicKey);
    }

    @Test
    public void encryptPayload() {

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

        when(enclave.encryptPayload(message, senderPublicKey, recipientPublicKeys)).thenReturn(encodedPayload);

        EncodedPayload result = enclaveAdapter.encryptPayload(message, senderPublicKey, recipientPublicKeys);

        assertThat(result).isNotNull();

    }

    @Test
    public void encryptRawTxnPayload() {

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

        when(enclave.encryptPayload(txn, Arrays.asList(senderPublicKey))).thenReturn(encodedPayload);

        EncodedPayload result = enclaveAdapter.encryptPayload(txn, Arrays.asList(senderPublicKey));

        assertThat(result).isNotNull();

    }

    @Test
    public void encryptRawPayload() {

        String key = "ROAZBWtSacxXQrOe3FGAqJDyJjFePR5ce4TSIzmJ0Bc=";
        PublicKey senderPublicKey = PublicKey.from(Base64.getDecoder().decode(key));

        byte[] message = "SOME MESSAGE".getBytes();

        RawTransaction txn = RawTransactionBuilder.create()
                .withEncryptedPayload(message)
                .withFrom(senderPublicKey).withEncryptedKey("PP".getBytes()).withNonce("nonce".getBytes()).build();

        when(enclave.encryptRawPayload(message, senderPublicKey)).thenReturn(txn);

        RawTransaction result = enclaveAdapter.encryptRawPayload(message, senderPublicKey);

        assertThat(result).isNotNull();

    }

    @Test
    public void unencryptTransaction() {

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

        when(enclave.unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(outcome);

        byte[] result = enclaveAdapter.unencryptTransaction(encodedPayload, providedKey);

        assertThat(result).isEqualTo(outcome);
    }

    @Test
    public void createBoxData() {

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

        when(enclave.createNewRecipientBox(any(EncodedPayload.class), any(PublicKey.class)))
                .thenReturn(outcome);

        byte[] result = enclaveAdapter.createNewRecipientBox(encodedPayload, providedKey);

        assertThat(result).isEqualTo(outcome);
    }

    //An impossible situation but for the last 0.01 coverage
    @Test(expected = UnsupportedOperationException.class)
    public void nullRequestType() {
        EnclaveEndpoint enclaveEndpoint = new EnclaveEndpoint();

        Session session = mock(Session.class);
        EnclaveRequest request = mock(EnclaveRequest.class);
        when(request.getType()).thenReturn(null);

        enclaveEndpoint.onRequest(session, request);

    }

    
    @Test
    public void status() {
        
        Service.Status status = Service.Status.STARTED;
        
        when(enclave.status()).thenReturn(status);

        Service.Status result = enclaveAdapter.status();

        assertThat(result).isEqualTo(status);
    }
    
}
