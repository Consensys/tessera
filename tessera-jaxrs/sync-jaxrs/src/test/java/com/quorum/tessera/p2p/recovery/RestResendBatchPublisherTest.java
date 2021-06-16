package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;

@RunWith(Parameterized.class)
public class RestResendBatchPublisherTest {

  private String targetUrl;

  private List<byte[]> payloadDatList;

  public RestResendBatchPublisherTest(Map.Entry<String, List<byte[]>> fixtures) {
    this.targetUrl = fixtures.getKey();
    this.payloadDatList = fixtures.getValue();
  }

  @Test
  public void publishBatch() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);

    RecoveryClient recoveryClient = mock(RecoveryClient.class);

    ArgumentCaptor<PushBatchRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(PushBatchRequest.class);
    when(recoveryClient.pushBatch(anyString(), requestArgumentCaptor.capture())).thenReturn(true);

    List<EncodedPayload> encodedPayloads =
        payloadDatList.stream()
            .map(
                o -> {
                  EncodedPayload encodedPayload = mock(EncodedPayload.class);
                  when(payloadEncoder.encode(encodedPayload)).thenReturn(o);
                  return encodedPayload;
                })
            .collect(Collectors.toList());

    RestResendBatchPublisher restRecoveryClient =
        new RestResendBatchPublisher(payloadEncoder, recoveryClient);
    restRecoveryClient.publishBatch(encodedPayloads, targetUrl);

    verify(recoveryClient).pushBatch(targetUrl, requestArgumentCaptor.getValue());

    encodedPayloads.forEach(
        p -> {
          verify(payloadEncoder).encode(p);
        });

    assertThat(requestArgumentCaptor.getValue().getEncodedPayloads()).isEqualTo(payloadDatList);

    verifyNoMoreInteractions(recoveryClient);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void publishBatchRecoveryClientFails() {

    PayloadEncoder payloadEncoder = mock(PayloadEncoder.class);

    RecoveryClient recoveryClient = mock(RecoveryClient.class);

    ArgumentCaptor<PushBatchRequest> requestArgumentCaptor =
        ArgumentCaptor.forClass(PushBatchRequest.class);
    when(recoveryClient.pushBatch(anyString(), requestArgumentCaptor.capture())).thenReturn(false);

    List<EncodedPayload> encodedPayloads =
        payloadDatList.stream()
            .map(
                o -> {
                  EncodedPayload encodedPayload = mock(EncodedPayload.class);
                  when(payloadEncoder.encode(encodedPayload)).thenReturn(o);
                  return encodedPayload;
                })
            .collect(Collectors.toList());

    RestResendBatchPublisher restRecoveryClient =
        new RestResendBatchPublisher(payloadEncoder, recoveryClient);
    PublishPayloadException ex =
        catchThrowableOfType(
            () -> restRecoveryClient.publishBatch(encodedPayloads, targetUrl),
            PublishPayloadException.class);

    assertThat(ex)
        .hasMessage(String.format("Unable to push payload batch to recipient %s", targetUrl));

    verify(recoveryClient).pushBatch(targetUrl, requestArgumentCaptor.getValue());

    encodedPayloads.forEach(
        p -> {
          verify(payloadEncoder).encode(p);
        });

    assertThat(requestArgumentCaptor.getValue().getEncodedPayloads()).isEqualTo(payloadDatList);

    verifyNoMoreInteractions(recoveryClient);
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Map.Entry<String, List<byte[]>>> fixtures() {
    return Map.of(
            "singlePayloadUrl",
            List.of("somePayloadData".getBytes()),
            "anotherMuliplePayloadUrl",
            IntStream.range(0, 100)
                .mapToObj(i -> UUID.randomUUID().toString().getBytes())
                .collect(Collectors.toList()))
        .entrySet();
  }
}
