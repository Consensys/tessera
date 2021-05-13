package com.quorum.tessera.p2p.recovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.jaxrs.mock.MockClient;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RestResendBatchPublisherTest {

  private MockClient restclient;

  private PayloadEncoder payloadEncoder;

  private RestResendBatchPublisher resendBatchPublisher;

  @Before
  public void onSetUp() {
    payloadEncoder = mock(PayloadEncoder.class);

    restclient = new MockClient();

    RecoveryClient recoveryClient = new RestRecoveryClient(restclient);

    resendBatchPublisher = new RestResendBatchPublisher(payloadEncoder, recoveryClient);
  }

  @After
  public void onTearDown() {
    verifyNoMoreInteractions(payloadEncoder);
  }

  @Test
  public void publishBatchSucess() {

    List<Entity> postedEntities = new ArrayList<>();

    Invocation.Builder invocationBuilder = restclient.getWebTarget().getMockInvocationBuilder();

    doAnswer(
            (invocation) -> {
              postedEntities.add(invocation.getArgument(0));
              return Response.ok().build();
            })
        .when(invocationBuilder)
        .post(any(javax.ws.rs.client.Entity.class));

    String targetUrl = "http://someplave.com/someresource";
    EncodedPayload payload = mock(EncodedPayload.class);
    List<EncodedPayload> payloads = Arrays.asList(payload);

    byte[] payloadData = "SOME DATA".getBytes();
    when(payloadEncoder.encode(payload)).thenReturn(payloadData);

    resendBatchPublisher.publishBatch(payloads, targetUrl);

    assertThat(postedEntities).hasSize(1);

    Entity entity = postedEntities.get(0);
    assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    assertThat(entity.getEntity()).isExactlyInstanceOf(PushBatchRequest.class);

    assertThat(PushBatchRequest.class.cast(entity.getEntity()).getEncodedPayloads())
        .containsExactly(payloadData);

    verify(payloadEncoder).encode(payload);
    verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
  }

  @Test
  public void publishBatchFailure() {

    List<Entity> postedEntities = new ArrayList<>();

    Invocation.Builder invocationBuilder = restclient.getWebTarget().getMockInvocationBuilder();

    doAnswer(
            (invocation) -> {
              postedEntities.add(invocation.getArgument(0));
              return Response.serverError().build();
            })
        .when(invocationBuilder)
        .post(any(javax.ws.rs.client.Entity.class));

    String targetUrl = "http://someplave.com/someresource";
    EncodedPayload payload = mock(EncodedPayload.class);
    List<EncodedPayload> payloads = Arrays.asList(payload);

    byte[] payloadData = "SOME DATA".getBytes();
    when(payloadEncoder.encode(payload)).thenReturn(payloadData);

    try {
      resendBatchPublisher.publishBatch(payloads, targetUrl);
      failBecauseExceptionWasNotThrown(PublishPayloadException.class);
    } catch (PublishPayloadException ex) {

      assertThat(postedEntities).hasSize(1);

      Entity entity = postedEntities.get(0);
      assertThat(entity.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
      assertThat(entity.getEntity()).isExactlyInstanceOf(PushBatchRequest.class);

      assertThat(PushBatchRequest.class.cast(entity.getEntity()).getEncodedPayloads())
          .containsExactly(payloadData);

      verify(payloadEncoder).encode(payload);
      verify(invocationBuilder).post(any(javax.ws.rs.client.Entity.class));
    }
  }

  @Test
  public void createMinimal() {
    assertThat(new RestResendBatchPublisher(new RestRecoveryClient(restclient))).isNotNull();
  }
}
