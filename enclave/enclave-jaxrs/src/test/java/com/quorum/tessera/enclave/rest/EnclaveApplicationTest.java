package com.quorum.tessera.enclave.rest;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static com.quorum.tessera.enclave.rest.Fixtures.createSample;
import com.quorum.tessera.service.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;

public class EnclaveApplicationTest {

    private Enclave enclave;

    private JerseyTest jersey;

    private RestfulEnclaveClient restfulEnclaveClient;

    @Before
    public void setUp() throws Exception {

        enclave = mock(Enclave.class);
        when(enclave.status()).thenReturn(Service.Status.STARTED);
        jersey = Util.create(enclave);

        jersey.setUp();

        restfulEnclaveClient = new RestfulEnclaveClient(jersey.client(), jersey.target().getUri());
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verifyNoMoreInteractions(enclave);
        jersey.tearDown();
    }

    @Test
    public void ping() throws Exception {

        Response response = jersey.target("ping").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isNotEmpty();
        verify(enclave).status();
    }

    @Test
    public void pingServerDown() throws Exception {

        when(enclave.status()).thenReturn(Service.Status.STOPPED);

        Response response = jersey.target("ping").request().get();
        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.readEntity(String.class)).isEqualTo(Service.Status.STOPPED.name());
        verify(enclave).status();
    }

    @Test
    public void encryptPayload() {
        byte[] message = "SOMEDATA".getBytes();
        EncodedPayload pay = createSample();

        List<byte[]> results = new ArrayList<>();
        doAnswer(
                        (invocation) -> {
                            results.add(invocation.getArgument(0));
                            return pay;
                        })
                .when(enclave)
                .encryptPayload(any(byte[].class), any(PublicKey.class), anyList());

        PublicKey senderPublicKey = pay.getSenderKey();
        List<PublicKey> recipientPublicKeys = pay.getRecipientKeys();

        EncodedPayload result = restfulEnclaveClient.encryptPayload(message, senderPublicKey, recipientPublicKeys);

        assertThat(result.getSenderKey()).isNotNull().isEqualTo(pay.getSenderKey());

        assertThat(result.getCipherTextNonce().getNonceBytes())
                .isNotNull()
                .isEqualTo(pay.getCipherTextNonce().getNonceBytes());

        assertThat(result.getCipherText()).isNotNull().isEqualTo(pay.getCipherText());

        assertThat(result.getRecipientKeys()).isNotNull().isEqualTo(pay.getRecipientKeys());

        byte[] resultBytes = PayloadEncoder.create().encode(result);

        assertThat(resultBytes).isEqualTo(PayloadEncoder.create().encode(pay));

        assertThat(results.get(0)).isEqualTo(message);

        verify(enclave).encryptPayload(any(byte[].class), any(PublicKey.class), anyList());
    }

    @Test
    public void unencryptPayload() {

        Map<PublicKey, EncodedPayload> payloads = new HashMap<>();

        doAnswer(
                        (invocation) -> {
                            payloads.put(invocation.getArgument(1), invocation.getArgument(0));
                            return "SOMERESULT".getBytes();
                        })
                .when(enclave)
                .unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));

        EncodedPayload payload = createSample();

        PublicKey providedKey = payload.getSenderKey();

        byte[] results = restfulEnclaveClient.unencryptTransaction(payload, providedKey);

        assertThat(results).isNotNull().isEqualTo("SOMERESULT".getBytes());

        assertThat(payloads).hasSize(1);

        assertThat(payloads.keySet().iterator().next()).isEqualTo(providedKey);

        EncodedPayload resultPayload = payloads.values().iterator().next();

        assertThat(resultPayload.getSenderKey()).isNotNull().isEqualTo(payload.getSenderKey());

        assertThat(resultPayload.getCipherTextNonce().getNonceBytes())
                .isNotNull()
                .isEqualTo(payload.getCipherTextNonce().getNonceBytes());

        assertThat(resultPayload.getCipherText()).isNotNull().isEqualTo(payload.getCipherText());

        assertThat(resultPayload.getRecipientKeys()).isNotNull().isEqualTo(payload.getRecipientKeys());

        verify(enclave).unencryptTransaction(any(EncodedPayload.class), any(PublicKey.class));
    }

    @Test
    public void appType() {
        assertThat(new EnclaveApplication(mock(EnclaveResource.class)).getAppType()).isEqualTo(AppType.ENCLAVE);
    }

    @Test
    public void getCommunicationType() {
        assertThat(new EnclaveApplication(mock(EnclaveResource.class)).getCommunicationType())
                .isEqualTo(CommunicationType.REST);
    }
}
