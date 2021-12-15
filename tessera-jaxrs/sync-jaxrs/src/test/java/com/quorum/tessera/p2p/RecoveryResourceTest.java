package com.quorum.tessera.p2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.EncodedPayloadCodec;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.p2p.recovery.PushBatchRequest;
import com.quorum.tessera.recovery.workflow.BatchResendManager;
import com.quorum.tessera.transaction.TransactionManager;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class RecoveryResourceTest {

  private RecoveryResource recoveryResource;

  private BatchResendManager resendManager;

  private TransactionManager transactionManager;

  private PayloadEncoder payloadEncoder;

  private final MockedStatic<PayloadEncoder> payloadEncoderFactoryFunction =
      mockStatic(PayloadEncoder.class);

  @Before
  public void onSetup() {
    resendManager = mock(BatchResendManager.class);
    transactionManager = mock(TransactionManager.class);
    payloadEncoder = mock(PayloadEncoder.class);
    payloadEncoderFactoryFunction
        .when(() -> PayloadEncoder.create(any(EncodedPayloadCodec.class)))
        .thenReturn(payloadEncoder);
    recoveryResource = new RecoveryResource(transactionManager, resendManager);
  }

  @After
  public void onTearDown() {
    try {
      verifyNoMoreInteractions(transactionManager, resendManager, payloadEncoder);
      payloadEncoderFactoryFunction.verifyNoMoreInteractions();
    } finally {
      payloadEncoderFactoryFunction.close();
    }
  }

  @Test
  public void pushBatch() {
    PushBatchRequest pushBatchRequest =
        new PushBatchRequest(Collections.singletonList("SomeData".getBytes()));
    Response result = recoveryResource.pushBatch(pushBatchRequest);
    assertThat(result.getStatus()).isEqualTo(200);
    ArgumentCaptor<com.quorum.tessera.recovery.resend.PushBatchRequest> argCaptor =
        ArgumentCaptor.forClass(com.quorum.tessera.recovery.resend.PushBatchRequest.class);
    verify(resendManager).storeResendBatch(argCaptor.capture());

    com.quorum.tessera.recovery.resend.PushBatchRequest capturedRequest = argCaptor.getValue();

    assertThat(capturedRequest).isNotNull();
    assertThat(capturedRequest.getEncodedPayloads()).containsExactly("SomeData".getBytes());
  }

  @Test
  public void pushAllowedForStandardPrivate() {
    final byte[] someData = "SomeData".getBytes();
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.STANDARD_PRIVATE);
    when(payloadEncoder.decode(someData)).thenReturn(payload);

    final Response result = recoveryResource.push(someData, null);

    assertThat(result.getStatus()).isEqualTo(201);
    assertThat(result.hasEntity()).isTrue();
    verify(transactionManager).storePayload(payload);
    verify(payloadEncoder).decode(someData);
    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }

  @Test
  public void pushNotAllowedForEnhancedPrivacy() {
    final byte[] someData = "SomeData".getBytes();
    final EncodedPayload payload = mock(EncodedPayload.class);
    when(payload.getPrivacyMode()).thenReturn(PrivacyMode.PRIVATE_STATE_VALIDATION);
    when(payloadEncoder.decode(someData)).thenReturn(payload);

    final Response result = recoveryResource.push(someData, null);

    assertThat(result.getStatus()).isEqualTo(403);
    verify(payloadEncoder).decode(someData);
    payloadEncoderFactoryFunction.verify(
        () -> PayloadEncoder.create(any(EncodedPayloadCodec.class)));
  }
}
