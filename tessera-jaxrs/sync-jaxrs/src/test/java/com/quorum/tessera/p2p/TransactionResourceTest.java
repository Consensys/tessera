package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.p2p.recovery.ResendBatchRequest;
import com.quorum.tessera.p2p.resend.ResendRequest;
import com.quorum.tessera.recovery.resend.ResendBatchResponse;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.recovery.workflow.LegacyResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.core.Response;
import java.util.Base64;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class TransactionResourceTest {

  private TransactionResource transactionResource;

  private TransactionManager transactionManager;

  private BatchResendManager batchResendManager;

  private PayloadEncoder payloadEncoder;

  private LegacyResendManager legacyResendManager;

  private final MockedStatic<PayloadEncoder> payloadEncoderFactoryFunction =
      mockStatic(PayloadEncoder.class);

  @Before
  public void onSetup() {
    transactionManager = mock(TransactionManager.class);
    batchResendManager = mock(BatchResendManager.class);
    payloadEncoder = mock(PayloadEncoder.class);
    legacyResendManager = mock(LegacyResendManager.class);

    payloadEncoder = mock(PayloadEncoder.class);
    payloadEncoderFactoryFunction
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(payloadEncoder);

    transactionResource =
        new TransactionResource(transactionManager, batchResendManager, legacyResendManager);
  }

  @After
  public void onTearDown() {
    try {
      verifyNoMoreInteractions(
          transactionManager, batchResendManager, payloadEncoder, legacyResendManager);
      payloadEncoderFactoryFunction.verifyNoMoreInteractions();
    } finally {
      payloadEncoderFactoryFunction.close();
    }
  }

  @Test
  public void push() {
    final byte[] someData = "SomeData".getBytes();
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payloadEncoder.decode(someData)).thenReturn(payload);

    final Response result = transactionResource.push(someData, List.of("4.0,5.0"));

    assertThat(result.getStatus()).isEqualTo(201);
    assertThat(result.hasEntity()).isTrue();
    verify(transactionManager).storePayload(payload);
    verify(payloadEncoder).decode(someData);

    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void resend() {
    ResendRequest resendRequest = new ResendRequest();
    resendRequest.setType("ALL");
    resendRequest.setPublicKey(Base64.getEncoder().encodeToString("JUNIT".getBytes()));

    EncodedPayload payload = mock(EncodedPayload.class);
    com.quorum.tessera.recovery.resend.ResendResponse resendResponse =
        mock(com.quorum.tessera.recovery.resend.ResendResponse.class);
    when(resendResponse.getPayload()).thenReturn(payload);

    when(legacyResendManager.resend(any(com.quorum.tessera.recovery.resend.ResendRequest.class)))
        .thenReturn(resendResponse);

    when(payloadEncoder.encode(payload)).thenReturn("SUCCESS".getBytes());

    Response result = transactionResource.resend(resendRequest);

    assertThat(result.getStatus()).isEqualTo(200);
    assertThat(result.getEntity()).isEqualTo("SUCCESS".getBytes());

    verify(payloadEncoder).encode(payload);
    verify(legacyResendManager).resend(any(com.quorum.tessera.recovery.resend.ResendRequest.class));

    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void resendBatch() {

    ResendBatchRequest incoming = new ResendBatchRequest();
    incoming.setPublicKey("someKey");
    incoming.setBatchSize(1);

    ResendBatchResponse resendResponse = ResendBatchResponse.from(1);
    when(batchResendManager.resendBatch(any())).thenReturn(resendResponse);

    Response result = transactionResource.resendBatch(incoming);
    assertThat(result.getStatus()).isEqualTo(200);
    com.quorum.tessera.p2p.recovery.ResendBatchResponse convertedResponse =
        (com.quorum.tessera.p2p.recovery.ResendBatchResponse) result.getEntity();

    assertThat(convertedResponse.getTotal()).isEqualTo(1);

    ArgumentCaptor<com.quorum.tessera.recovery.resend.ResendBatchRequest> captor =
        ArgumentCaptor.forClass(com.quorum.tessera.recovery.resend.ResendBatchRequest.class);

    verify(batchResendManager).resendBatch(captor.capture());

    com.quorum.tessera.recovery.resend.ResendBatchRequest convertedRequest = captor.getValue();

    assertThat(convertedRequest.getPublicKey()).isEqualTo("someKey");
    assertThat(convertedRequest.getBatchSize()).isEqualTo(1);
  }
}
