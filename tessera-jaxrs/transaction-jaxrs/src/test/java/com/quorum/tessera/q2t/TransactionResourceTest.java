package com.quorum.tessera.q2t;

import com.quorum.tessera.api.*;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.ReceiveResponse;
import com.quorum.tessera.transaction.TransactionManager;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TransactionResourceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResourceTest.class);

    private JerseyTest jersey;

    private TransactionManager transactionManager;

    @BeforeClass
    public static void setUpLoggers() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Before
    public void onSetup() throws Exception {

        transactionManager = mock(TransactionManager.class);
        TransactionResource transactionResource = new TransactionResource(transactionManager);

        jersey =
            new JerseyTest() {
                @Override
                protected Application configure() {
                    forceSet(TestProperties.CONTAINER_PORT, "0");
                    enable(TestProperties.LOG_TRAFFIC);
                    enable(TestProperties.DUMP_ENTITY);

                    return ResourceConfig.forApplication(new Application() {
                        @Override
                        public Set<Object> getSingletons() {
                            return Set.of(transactionResource, (ExceptionMapper<Exception>) exception -> {
                                LOGGER.error("",exception);
                                return Response.serverError().build();
                            });
                        }
                    });
                }
            };



        jersey.setUp();

    }

    @After
    public void onTearDown() throws Exception {
        verifyNoMoreInteractions(transactionManager);
        jersey.tearDown();

    }

    @Ignore
    @Test
    public void receive() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);

        String recipient = Base64.getEncoder().encodeToString("Bobby Sixkiller".getBytes());

        receiveRequest.setTo(recipient);

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);

        when(receiveResponse.getAffectedTransactions()).thenReturn(Set.of());
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());
        when(receiveResponse.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(receiveResponse);

        final Response result = jersey
            .target("receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse = result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);

        assertThat(resultResponse.getExecHash()).isNull();
        assertThat(resultResponse.getPrivacyFlag()).isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));

    }

    @Ignore
    @Test
    public void receiveWithRecipient() {
        String key = Base64.getEncoder().encodeToString("KEY".getBytes());
        ReceiveRequest receiveRequest = new ReceiveRequest();
        receiveRequest.setKey(key);
        receiveRequest.setTo(Base64.getEncoder().encodeToString("Reno Raynes".getBytes()));

        ReceiveResponse receiveResponse = mock(ReceiveResponse.class);
        when(receiveResponse.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(transactionManager.receive(any())).thenReturn(receiveResponse);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn("Result".getBytes());

        Response result = jersey
            .target("receive")
            .request()
            .post(Entity.entity(receiveRequest, MediaType.APPLICATION_JSON));


        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse = result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);
        assertThat(resultResponse.getPrivacyFlag()).isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());
        assertThat(resultResponse.getExecHash()).isNull();

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));

    }

    @Test
    public void receiveFromParamsPrivateStateValidation() {

        com.quorum.tessera.transaction.ReceiveResponse response = mock(com.quorum.tessera.transaction.ReceiveResponse.class);
        when(response.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
        when(response.getAffectedTransactions()).thenReturn(Collections.emptySet());
        when(response.getUnencryptedTransactionData()).thenReturn("Success".getBytes());
        when(response.getExecHash()).thenReturn("execHash".getBytes());

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(response);

        String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

        Response result = jersey
            .target("transaction")
            .path(transactionHash)
            .request()
            .get();

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse = result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);

        assertThat(resultResponse.getExecHash()).isEqualTo("execHash");
        assertThat(resultResponse.getPrivacyFlag())
            .isEqualTo(PrivacyMode.PRIVATE_STATE_VALIDATION.getPrivacyFlag());

        assertThat(resultResponse.getAffectedContractTransactions()).isNullOrEmpty();
        assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));

    }

    @Test
    public void receiveFromParams() {

        com.quorum.tessera.transaction.ReceiveResponse response = mock(com.quorum.tessera.transaction.ReceiveResponse.class);
        when(response.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
        when(response.getUnencryptedTransactionData()).thenReturn("Success".getBytes());

        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class))).thenReturn(response);

        String transactionHash = Base64.getEncoder().encodeToString("transactionHash".getBytes());

        Response result = jersey
            .target("transaction")
            .path(transactionHash)
            .request()
            .get();

        assertThat(result.getStatus()).isEqualTo(200);

        com.quorum.tessera.api.ReceiveResponse resultResponse = result.readEntity(com.quorum.tessera.api.ReceiveResponse.class);
        assertThat(resultResponse.getExecHash()).isNull();
        assertThat(resultResponse.getPrivacyFlag())
            .isEqualTo(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

        assertThat(resultResponse.getExecHash()).isNull();
        assertThat(resultResponse.getAffectedContractTransactions()).isNull();
        assertThat(resultResponse.getPayload()).isEqualTo("Success".getBytes());

        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));

    }

    @Test
    public void receiveRaw() {

        byte[] encodedPayload = Base64.getEncoder().encode("Payload".getBytes());
        com.quorum.tessera.transaction.ReceiveResponse receiveResponse =
                mock(com.quorum.tessera.transaction.ReceiveResponse.class);
        when(receiveResponse.getUnencryptedTransactionData()).thenReturn(encodedPayload);
        when(transactionManager.receive(any(com.quorum.tessera.transaction.ReceiveRequest.class)))
                .thenReturn(receiveResponse);

        final Response result = jersey
            .target("receiveraw")
            .request()
            .header("c11n-key","")
            .header("c11n-to","")
            .get();

        assertThat(result.getStatus()).isEqualTo(200);
        verify(transactionManager).receive(any(com.quorum.tessera.transaction.ReceiveRequest.class));

    }

    @Test
    public void send() throws Exception {

        final Base64.Encoder base64Encoder = Base64.getEncoder();

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
        sendRequest.setPrivacyFlag(PrivacyMode.STANDARD_PRIVATE.getPrivacyFlag());

        final PublicKey sender = mock(PublicKey.class);
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        final MessageHash messageHash = mock(MessageHash.class);

        final byte[] txnData = "TxnData".getBytes();
        when(messageHash.getHashBytes()).thenReturn(txnData);

        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        final Response result = jersey
                                .target("send")
                                .request()
                                .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));
        SendResponse resultSendResponse = result.readEntity(SendResponse.class);
        assertThat(resultSendResponse.getKey());

        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendForRecipient() throws Exception {

        final Base64.Encoder base64Encoder = Base64.getEncoder();

        final SendRequest sendRequest = new SendRequest();
        sendRequest.setPayload(base64Encoder.encode("PAYLOAD".getBytes()));
        sendRequest.setTo(Base64.getEncoder().encodeToString("Mr Benn".getBytes()));
        final PublicKey sender = mock(PublicKey.class);
        when(transactionManager.defaultPublicKey()).thenReturn(sender);

        final com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        final MessageHash messageHash = mock(MessageHash.class);

        final byte[] txnData = "TxnData".getBytes();
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);


        final Response result = jersey
            .target("send")
            .request()
            .post(Entity.entity(sendRequest, MediaType.APPLICATION_JSON));

        assertThat(result.getStatus()).isEqualTo(201);

        assertThat(result.getLocation().getPath()).isEqualTo("/transaction/" + base64Encoder.encodeToString(txnData));

        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendSignedTransactionWithRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        String recipentKey = Optional.of("RECIPIENT".getBytes()).map(Base64.getEncoder()::encodeToString).get();

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        Response result = jersey
            .target("sendsignedtx")
            .request()
            .header("c11n-to",recipentKey)
            .post(Entity.entity(txnData, MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransactionEmptyRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        Response result = jersey
            .target("sendsignedtx")
            .request()
            .header("c11n-to","")
            .post(Entity.entity("".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransactionNullRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        Response result = jersey
            .target("sendsignedtx")
            .request()
            .header("c11n-to",null)
            .post(Entity.entity("".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString("KEY".getBytes()));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendSignedTransaction() throws Exception {

        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);

        byte[] transactionHashData = "I Love Sparrows".getBytes();
        final String base64EncodedTransactionHAshData = Base64.getEncoder().encodeToString(transactionHashData);
        MessageHash transactionHash = mock(MessageHash.class);
        when(transactionHash.getHashBytes()).thenReturn(transactionHashData);

        when(sendResponse.getTransactionHash()).thenReturn(transactionHash);

        com.quorum.tessera.transaction.SendSignedRequest sendRequest =
                mock(com.quorum.tessera.transaction.SendSignedRequest.class);

        when(transactionManager.sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class)))
                .thenReturn(sendResponse);

        SendSignedRequest sendSignedRequest = new SendSignedRequest();
        sendSignedRequest.setHash("SOMEDATA".getBytes());

        Response result = jersey
            .target("sendsignedtx")
            .request()
            .post(Entity.entity(sendSignedRequest, MediaType.APPLICATION_JSON_TYPE));

        assertThat(result.getStatus()).isEqualTo(201);

        SendResponse resultResponse = result.readEntity(SendResponse.class);

        assertThat(resultResponse.getKey()).isEqualTo(base64EncodedTransactionHAshData);

        assertThat(result.getLocation()).hasPath("/transaction/".concat(base64EncodedTransactionHAshData));
        verify(transactionManager).sendSignedTransaction(any(com.quorum.tessera.transaction.SendSignedRequest.class));
    }

    @Test
    public void sendRaw() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));

        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = jersey
            .target("sendraw")
            .request()
            .header("c11n-from","")
            .header("c11n-to","someone")
            .post(Entity.entity("".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendRawEmptyRecipients() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);

        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = jersey
            .target("sendraw")
            .request()
            .header("c11n-from","")
            .header("c11n-to","")
            .post(Entity.entity("".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));


        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void sendRawNullRecipient() throws UnsupportedEncodingException {

        byte[] txnData = "KEY".getBytes();
        com.quorum.tessera.transaction.SendResponse sendResponse =
                mock(com.quorum.tessera.transaction.SendResponse.class);
        MessageHash messageHash = mock(MessageHash.class);
        when(messageHash.getHashBytes()).thenReturn(txnData);
        when(sendResponse.getTransactionHash()).thenReturn(messageHash);
        when(transactionManager.defaultPublicKey()).thenReturn(mock(PublicKey.class));
        when(transactionManager.send(any(com.quorum.tessera.transaction.SendRequest.class))).thenReturn(sendResponse);

        Response result = jersey
            .target("sendraw")
            .request()
            .header("c11n-from","")
            .header("c11n-to",null)
            .post(Entity.entity("".getBytes(), MediaType.APPLICATION_OCTET_STREAM_TYPE));

        assertThat(result.getStatus()).isEqualTo(200);
        assertThat(result.readEntity(String.class)).isEqualTo(Base64.getEncoder().encodeToString(txnData));
        verify(transactionManager).send(any(com.quorum.tessera.transaction.SendRequest.class));
        verify(transactionManager).defaultPublicKey();
    }

    @Test
    public void deleteKey() {

        String encodedTxnHash = Base64.getEncoder().encodeToString("KEY".getBytes());
        List<MessageHash> results = new ArrayList<>();
        doAnswer((iom) -> results.add(iom.getArgument(0))).when(transactionManager).delete(any(MessageHash.class));

        Response response = jersey
            .target("transaction").path(encodedTxnHash)
            .request()
            .delete();

        assertThat(results).hasSize(1).extracting(MessageHash::getHashBytes).containsExactly("KEY".getBytes());

        assertThat(response.getStatus()).isEqualTo(204);

        verify(transactionManager).delete(any(MessageHash.class));
    }

    @Test
    public void delete() {

        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setKey("KEY");

        Response response = jersey.target("delete")
            .request()
            .post(Entity.entity(deleteRequest,MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("Delete successful");
        verify(transactionManager).delete(any(MessageHash.class));
    }

    @Test
    public void isSenderDelegates() {

        when(transactionManager.isSender(any(MessageHash.class))).thenReturn(true);

        String senderKey = Base64.getEncoder().encodeToString("DUMMY_HASH".getBytes());

        Response response = jersey.target("transaction")
            .path(senderKey)
            .path("isSender")
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Boolean.class)).isEqualTo(true);
        verify(transactionManager).isSender(any(MessageHash.class));
    }

    @Test
    public void getParticipantsDelegates() {
        byte[] data = "DUMMY_HASH".getBytes();

        final String dummyPtmHash = Base64.getEncoder().encodeToString(data);

        PublicKey recipient = mock(PublicKey.class);
        when(recipient.encodeToBase64()).thenReturn("BASE64ENCODEKEY");

        when(transactionManager.getParticipants(any(MessageHash.class))).thenReturn(List.of(recipient));

        Response response = jersey.target("transaction")
            .path(dummyPtmHash).path("participants")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("BASE64ENCODEKEY");
        verify(transactionManager).getParticipants(any(MessageHash.class));
    }
}
