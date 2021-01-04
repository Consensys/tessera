package com.quorum.tessera.q2t;

import com.quorum.tessera.api.PayloadDecryptRequest;
import com.quorum.tessera.api.PayloadEncryptResponse;
import com.quorum.tessera.api.SendRequest;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.EncodedPayloadManager;
import com.quorum.tessera.transaction.ReceiveResponse;
import com.quorum.tessera.transaction.TransactionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.quorum.tessera.version.MultiTenancyVersion.MIME_TYPE_JSON_2_1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EncodedPayloadResourceTest {

    private JerseyTest jersey;

    private TransactionManager transactionManager;

    private EncodedPayloadManager encodedPayloadManager;

    @BeforeClass
    public static void setUpLoggers() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void onSetup() throws Exception {
        this.transactionManager = mock(TransactionManager.class);
        this.encodedPayloadManager = mock(EncodedPayloadManager.class);

        final EncodedPayloadResource encodedPayloadResource =
                new EncodedPayloadResource(encodedPayloadManager, transactionManager);

        this.jersey =
                new JerseyTest() {
                    @Override
                    protected Application configure() {
                        forceSet(TestProperties.CONTAINER_PORT, "0");
                        enable(TestProperties.LOG_TRAFFIC);
                        enable(TestProperties.DUMP_ENTITY);
                        return new ResourceConfig().register(encodedPayloadResource);
                    }
                };
        this.jersey.setUp();
    }

    @After
    public void onTearDown() throws Exception {
        verifyNoMoreInteractions(transactionManager);

        this.jersey.tearDown();
    }

    @Test
    public void createPayload() {
        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(Base64.getEncoder().encode("PAYLOAD".getBytes()));
        sendRequest.setTo(base64Key);
        sendRequest.setAffectedContractTransactions("dHgx");

        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("oNspPPgszVUFw0qmGFfWwh1uxVUXgvBxleXORHj07g8="));
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final EncodedPayload samplePayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(sender)
                        .withRecipientKeys(List.of(PublicKey.from(Base64.getDecoder().decode(base64Key))))
                        .withRecipientBoxes(List.of("boxOne".getBytes()))
                        .withRecipientNonce("recipientNonce".getBytes())
                        .withCipherText("testPayload".getBytes())
                        .withCipherTextNonce("cipherTextNonce".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(Map.of(TxHash.from("tx1".getBytes()), "tx1val".getBytes()))
                        .withExecHash(new byte[0])
                        .build();

        when(encodedPayloadManager.create(any(com.quorum.tessera.transaction.SendRequest.class)))
                .thenReturn(samplePayload);

        final Response result =
                jersey.target("/encodedpayload/create")
                        .request()
                        .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(200);

        final PayloadEncryptResponse payloadEncryptResponse = result.readEntity(PayloadEncryptResponse.class);
        assertThat(PublicKey.from(payloadEncryptResponse.getSenderKey())).isEqualTo(sender);
        assertThat(payloadEncryptResponse.getCipherText()).isEqualTo("testPayload".getBytes());
        assertThat(payloadEncryptResponse.getCipherTextNonce()).isEqualTo("cipherTextNonce".getBytes());
        assertThat(payloadEncryptResponse.getRecipientBoxes()).hasSize(1).containsExactly("boxOne".getBytes());
        assertThat(payloadEncryptResponse.getRecipientNonce()).isEqualTo("recipientNonce".getBytes());
        assertThat(payloadEncryptResponse.getRecipientKeys()).hasSize(1);
        assertThat(payloadEncryptResponse.getRecipientKeys().get(0)).isEqualTo(Base64.getDecoder().decode(base64Key));
        assertThat(payloadEncryptResponse.getPrivacyMode()).isEqualTo(0);
        assertThat(payloadEncryptResponse.getAffectedContractTransactions()).contains(entry("dHgx", "dHgxdmFs"));
        assertThat(payloadEncryptResponse.getExecHash()).isEmpty();

        final ArgumentCaptor<com.quorum.tessera.transaction.SendRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendRequest.class);

        verify(encodedPayloadManager).create(argumentCaptor.capture());
        verify(transactionManager).defaultPublicKey();

        com.quorum.tessera.transaction.SendRequest businessObject = argumentCaptor.getValue();
        assertThat(businessObject).isNotNull();
        assertThat(businessObject.getPayload()).isEqualTo(sendRequest.getPayload());
        assertThat(businessObject.getSender()).isEqualTo(sender);
        assertThat(businessObject.getRecipients()).hasSize(1);
        assertThat(businessObject.getRecipients().get(0).encodeToBase64()).isEqualTo(base64Key);
        assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(businessObject.getAffectedContractTransactions()).containsExactly(new MessageHash("tx1".getBytes()));
        assertThat(businessObject.getExecHash()).isEmpty();
    }

    @Test
    public void createPayloadNullPayload() {
        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(base64Key);

        final String sampleBadRequest = "{}";
        final Response result =
                jersey.target("/encodedpayload/create")
                        .request()
                        .post(Entity.entity(sampleBadRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(400);
    }

    @Test
    public void decryptPayload() {
        final Base64.Decoder decoder = Base64.getDecoder();

        final PayloadDecryptRequest request = new PayloadDecryptRequest();
        request.setSenderKey(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        request.setCipherText(decoder.decode("h7av/vhPlaPFECB1K30hNWugv/Bu"));
        request.setCipherTextNonce(decoder.decode("8MVXAESCQuRHWxrQ6b5MXuYApjia+2h0"));
        request.setRecipientBoxes(
                List.of(decoder.decode("FNirZRc2ayMaYopCBaWQ/1I7VWFiCM0lNw533Hckzxb+qpvngdWVVzJlsE05dbxl")));
        request.setRecipientNonce(decoder.decode("p9gYDJlEoBvLdUQ+ZoONl2Jl9AirV1en"));
        request.setRecipientKeys(List.of(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")));
        request.setPrivacyMode(0);
        request.setAffectedContractTransactions(Map.of("dHgx", "dHgxdmFs", "dHgy", "dHgydmFs"));
        request.setExecHash("execHash".getBytes());

        final ReceiveResponse response =
                ReceiveResponse.Builder.create()
                        .withUnencryptedTransactionData("decryptedData".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedTransactions(
                                Set.of(new MessageHash("tx1val".getBytes()), new MessageHash("tx2val".getBytes())))
                        .withExecHash("execHash".getBytes())
                        .withSender(PublicKey.from(request.getSenderKey()))
                        .build();

        when(encodedPayloadManager.decrypt(any(), eq(null))).thenReturn(response);

        final Response result =
                jersey.target("/encodedpayload/decrypt")
                        .request()
                        .post(Entity.entity(request, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(200);

        final com.quorum.tessera.api.ReceiveResponse payloadEncryptResponse =
                result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);
        assertThat(payloadEncryptResponse.getPayload()).isEqualTo("decryptedData".getBytes());
        assertThat(payloadEncryptResponse.getPrivacyFlag()).isEqualTo(0);
        assertThat(payloadEncryptResponse.getAffectedContractTransactions())
                .containsExactlyInAnyOrder("dHgxdmFs", "dHgydmFs");
        assertThat(payloadEncryptResponse.getExecHash()).isEqualTo("execHash");

        final ArgumentCaptor<EncodedPayload> argumentCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(encodedPayloadManager).decrypt(argumentCaptor.capture(), eq(null));

        final EncodedPayload payloadBeforeDecryption = argumentCaptor.getValue();
        assertThat(payloadBeforeDecryption.getSenderKey().encodeToBase64())
                .isEqualTo("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");
        assertThat(payloadBeforeDecryption.getCipherText()).isEqualTo(decoder.decode("h7av/vhPlaPFECB1K30hNWugv/Bu"));
        assertThat(payloadBeforeDecryption.getCipherTextNonce().getNonceBytes())
                .isEqualTo(decoder.decode("8MVXAESCQuRHWxrQ6b5MXuYApjia+2h0"));
        assertThat(payloadBeforeDecryption.getRecipientBoxes())
                .containsExactly(
                        RecipientBox.from(
                                decoder.decode("FNirZRc2ayMaYopCBaWQ/1I7VWFiCM0lNw533Hckzxb+qpvngdWVVzJlsE05dbxl")));
        assertThat(payloadBeforeDecryption.getRecipientNonce().getNonceBytes())
                .isEqualTo(decoder.decode("p9gYDJlEoBvLdUQ+ZoONl2Jl9AirV1en"));
        assertThat(payloadBeforeDecryption.getRecipientKeys())
                .containsExactly(PublicKey.from(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")));
        assertThat(payloadBeforeDecryption.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(payloadBeforeDecryption.getAffectedContractTransactions())
                .contains(
                        entry(TxHash.from("tx1".getBytes()), SecurityHash.from("tx1val".getBytes())),
                        entry(TxHash.from("tx2".getBytes()), SecurityHash.from("tx2val".getBytes())));
        assertThat(payloadBeforeDecryption.getExecHash()).isEqualTo("execHash".getBytes());
    }

    @Test
    public void createPayloadVersion21() {
        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(Base64.getEncoder().encode("PAYLOAD".getBytes()));
        sendRequest.setTo(base64Key);
        sendRequest.setAffectedContractTransactions("dHgx");

        final PublicKey sender =
                PublicKey.from(Base64.getDecoder().decode("oNspPPgszVUFw0qmGFfWwh1uxVUXgvBxleXORHj07g8="));
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final EncodedPayload samplePayload =
                EncodedPayload.Builder.create()
                        .withSenderKey(sender)
                        .withRecipientKeys(List.of(PublicKey.from(Base64.getDecoder().decode(base64Key))))
                        .withRecipientBoxes(List.of("boxOne".getBytes()))
                        .withRecipientNonce("recipientNonce".getBytes())
                        .withCipherText("testPayload".getBytes())
                        .withCipherTextNonce("cipherTextNonce".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedContractTransactions(Map.of(TxHash.from("tx1".getBytes()), "tx1val".getBytes()))
                        .withExecHash(new byte[0])
                        .build();

        when(encodedPayloadManager.create(any(com.quorum.tessera.transaction.SendRequest.class)))
                .thenReturn(samplePayload);

        final Response result =
                jersey.target("/encodedpayload/create").request().post(Entity.entity(sendRequest, MIME_TYPE_JSON_2_1));

        assertThat(result.getStatus()).isEqualTo(200);

        final PayloadEncryptResponse payloadEncryptResponse = result.readEntity(PayloadEncryptResponse.class);
        assertThat(PublicKey.from(payloadEncryptResponse.getSenderKey())).isEqualTo(sender);
        assertThat(payloadEncryptResponse.getCipherText()).isEqualTo("testPayload".getBytes());
        assertThat(payloadEncryptResponse.getCipherTextNonce()).isEqualTo("cipherTextNonce".getBytes());
        assertThat(payloadEncryptResponse.getRecipientBoxes()).hasSize(1).containsExactly("boxOne".getBytes());
        assertThat(payloadEncryptResponse.getRecipientNonce()).isEqualTo("recipientNonce".getBytes());
        assertThat(payloadEncryptResponse.getRecipientKeys()).hasSize(1);
        assertThat(payloadEncryptResponse.getRecipientKeys().get(0)).isEqualTo(Base64.getDecoder().decode(base64Key));
        assertThat(payloadEncryptResponse.getPrivacyMode()).isEqualTo(0);
        assertThat(payloadEncryptResponse.getAffectedContractTransactions()).contains(entry("dHgx", "dHgxdmFs"));
        assertThat(payloadEncryptResponse.getExecHash()).isEmpty();

        final ArgumentCaptor<com.quorum.tessera.transaction.SendRequest> argumentCaptor =
                ArgumentCaptor.forClass(com.quorum.tessera.transaction.SendRequest.class);

        verify(encodedPayloadManager).create(argumentCaptor.capture());
        verify(transactionManager).defaultPublicKey();

        com.quorum.tessera.transaction.SendRequest businessObject = argumentCaptor.getValue();
        assertThat(businessObject).isNotNull();
        assertThat(businessObject.getPayload()).isEqualTo(sendRequest.getPayload());
        assertThat(businessObject.getSender()).isEqualTo(sender);
        assertThat(businessObject.getRecipients()).hasSize(1);
        assertThat(businessObject.getRecipients().get(0).encodeToBase64()).isEqualTo(base64Key);
        assertThat(businessObject.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(businessObject.getAffectedContractTransactions()).containsExactly(new MessageHash("tx1".getBytes()));
        assertThat(businessObject.getExecHash()).isEmpty();
    }

    @Test
    public void createPayloadNullPayloadVersion21() {
        final String base64Key = "BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=";

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setFrom(base64Key);

        final String sampleBadRequest = "{}";
        final Response result =
                jersey.target("/encodedpayload/create")
                        .request()
                        .post(Entity.entity(sampleBadRequest, MIME_TYPE_JSON_2_1));

        assertThat(result.getStatus()).isEqualTo(400);
    }

    @Test
    public void decryptPayloadVersion21() {
        final Base64.Decoder decoder = Base64.getDecoder();

        final PayloadDecryptRequest request = new PayloadDecryptRequest();
        request.setSenderKey(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo="));
        request.setCipherText(decoder.decode("h7av/vhPlaPFECB1K30hNWugv/Bu"));
        request.setCipherTextNonce(decoder.decode("8MVXAESCQuRHWxrQ6b5MXuYApjia+2h0"));
        request.setRecipientBoxes(
                List.of(decoder.decode("FNirZRc2ayMaYopCBaWQ/1I7VWFiCM0lNw533Hckzxb+qpvngdWVVzJlsE05dbxl")));
        request.setRecipientNonce(decoder.decode("p9gYDJlEoBvLdUQ+ZoONl2Jl9AirV1en"));
        request.setRecipientKeys(List.of(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")));
        request.setPrivacyMode(0);
        request.setAffectedContractTransactions(Map.of("dHgx", "dHgxdmFs", "dHgy", "dHgydmFs"));
        request.setExecHash("execHash".getBytes());

        final ReceiveResponse response =
                ReceiveResponse.Builder.create()
                        .withUnencryptedTransactionData("decryptedData".getBytes())
                        .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
                        .withAffectedTransactions(
                                Set.of(new MessageHash("tx1val".getBytes()), new MessageHash("tx2val".getBytes())))
                        .withExecHash("execHash".getBytes())
                        .withSender(PublicKey.from(request.getSenderKey()))
                        .build();

        when(encodedPayloadManager.decrypt(any(), eq(null))).thenReturn(response);

        final Response result =
                jersey.target("/encodedpayload/decrypt").request().post(Entity.entity(request, MIME_TYPE_JSON_2_1));

        assertThat(result.getStatus()).isEqualTo(200);

        final com.quorum.tessera.api.ReceiveResponse payloadEncryptResponse =
                result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);
        assertThat(payloadEncryptResponse.getPayload()).isEqualTo("decryptedData".getBytes());
        assertThat(payloadEncryptResponse.getPrivacyFlag()).isEqualTo(0);
        assertThat(payloadEncryptResponse.getAffectedContractTransactions())
                .containsExactlyInAnyOrder("dHgxdmFs", "dHgydmFs");
        assertThat(payloadEncryptResponse.getExecHash()).isEqualTo("execHash");

        final ArgumentCaptor<EncodedPayload> argumentCaptor = ArgumentCaptor.forClass(EncodedPayload.class);
        verify(encodedPayloadManager).decrypt(argumentCaptor.capture(), eq(null));

        final EncodedPayload payloadBeforeDecryption = argumentCaptor.getValue();
        assertThat(payloadBeforeDecryption.getSenderKey().encodeToBase64())
                .isEqualTo("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=");
        assertThat(payloadBeforeDecryption.getCipherText()).isEqualTo(decoder.decode("h7av/vhPlaPFECB1K30hNWugv/Bu"));
        assertThat(payloadBeforeDecryption.getCipherTextNonce().getNonceBytes())
                .isEqualTo(decoder.decode("8MVXAESCQuRHWxrQ6b5MXuYApjia+2h0"));
        assertThat(payloadBeforeDecryption.getRecipientBoxes())
                .containsExactly(
                        RecipientBox.from(
                                decoder.decode("FNirZRc2ayMaYopCBaWQ/1I7VWFiCM0lNw533Hckzxb+qpvngdWVVzJlsE05dbxl")));
        assertThat(payloadBeforeDecryption.getRecipientNonce().getNonceBytes())
                .isEqualTo(decoder.decode("p9gYDJlEoBvLdUQ+ZoONl2Jl9AirV1en"));
        assertThat(payloadBeforeDecryption.getRecipientKeys())
                .containsExactly(PublicKey.from(decoder.decode("BULeR8JyUWhiuuCMU/HLA0Q5pzkYT+cHII3ZKBey3Bo=")));
        assertThat(payloadBeforeDecryption.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
        assertThat(payloadBeforeDecryption.getAffectedContractTransactions())
                .contains(
                        entry(TxHash.from("tx1".getBytes()), SecurityHash.from("tx1val".getBytes())),
                        entry(TxHash.from("tx2".getBytes()), SecurityHash.from("tx2val".getBytes())));
        assertThat(payloadBeforeDecryption.getExecHash()).isEqualTo("execHash".getBytes());
    }
}
