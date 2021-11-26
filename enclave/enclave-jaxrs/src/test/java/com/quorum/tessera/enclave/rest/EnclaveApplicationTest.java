package com.quorum.tessera.enclave.rest;

import static com.quorum.tessera.enclave.rest.Fixtures.createSample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import com.quorum.tessera.config.AppType;
import com.quorum.tessera.config.CommunicationType;
import com.quorum.tessera.enclave.*;
import com.quorum.tessera.encryption.Nonce;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.service.Service;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EnclaveApplicationTest {

  private final EncodedPayloadCodec encodedPayloadCodec = EncodedPayloadCodec.LEGACY;

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
        .encryptPayload(any(byte[].class), any(PublicKey.class), anyList(), any());

    PublicKey senderPublicKey = pay.getSenderKey();
    List<PublicKey> recipientPublicKeys = pay.getRecipientKeys();
    EncodedPayload acoth = mock(EncodedPayload.class);
    when(acoth.getSenderKey()).thenReturn(senderPublicKey);
    when(acoth.getCipherText()).thenReturn("ciphertext".getBytes());
    when(acoth.getCipherTextNonce()).thenReturn(new Nonce("0".getBytes()));
    when(acoth.getRecipientKeys()).thenReturn(Collections.emptyList());
    when(acoth.getRecipientNonce()).thenReturn(new Nonce("0".getBytes()));
    when(acoth.getRecipientBoxes()).thenReturn(Collections.emptyList());
    when(acoth.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(acoth.getAffectedContractTransactions()).thenReturn(Collections.emptyMap());
    when(acoth.getExecHash()).thenReturn("0".getBytes());

    TxHash txHash = new TxHash("key".getBytes());
    AffectedTransaction affectedTransaction = mock(AffectedTransaction.class);
    when(affectedTransaction.getPayload()).thenReturn(acoth);
    when(affectedTransaction.getHash()).thenReturn(txHash);

    PrivacyMetadata privacyMetaData =
        PrivacyMetadata.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withAffectedTransactions(List.of(affectedTransaction))
            .build();

    EncodedPayload result =
        restfulEnclaveClient.encryptPayload(
            message, senderPublicKey, recipientPublicKeys, privacyMetaData);

    assertThat(result.getSenderKey()).isNotNull().isEqualTo(pay.getSenderKey());

    assertThat(result.getCipherTextNonce().getNonceBytes())
        .isNotNull()
        .isEqualTo(pay.getCipherTextNonce().getNonceBytes());

    assertThat(result.getCipherText()).isNotNull().isEqualTo(pay.getCipherText());

    assertThat(result.getRecipientKeys()).isNotNull().isEqualTo(pay.getRecipientKeys());

    PayloadEncoder payloadEncoder = PayloadEncoder.create(encodedPayloadCodec);
    byte[] resultBytes = payloadEncoder.encode(result);

    assertThat(resultBytes).isEqualTo(payloadEncoder.encode(pay));

    assertThat(results.get(0)).isEqualTo(message);

    verify(enclave)
        .encryptPayload(
            any(byte[].class), any(PublicKey.class), anyList(), any(PrivacyMetadata.class));
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
    assertThat(new EnclaveApplication(enclave).getAppType()).isEqualTo(AppType.ENCLAVE);
  }

  @Test
  public void getCommunicationType() {
    assertThat(new EnclaveApplication(enclave).getCommunicationType())
        .isEqualTo(CommunicationType.REST);
  }

  @Test
  public void defaultConstructor() throws Exception {

    try (var enclaveMockedStatic = mockStatic(EnclaveServer.class)) {
      enclaveMockedStatic.when(EnclaveServer::create).thenReturn(mock(EnclaveServer.class));
      new EnclaveApplication();
      enclaveMockedStatic.verify(EnclaveServer::create);
      enclaveMockedStatic.verifyNoMoreInteractions();
    }
  }
}
