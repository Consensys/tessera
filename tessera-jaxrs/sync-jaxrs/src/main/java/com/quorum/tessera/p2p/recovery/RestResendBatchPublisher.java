package com.quorum.tessera.p2p.recovery;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.recovery.resend.ResendBatchPublisher;
import com.quorum.tessera.transaction.publish.PublishPayloadException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestResendBatchPublisher implements ResendBatchPublisher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestResendBatchPublisher.class);

  private final PayloadEncoder payloadEncoder;

  private final RecoveryClient resendClient;

  public RestResendBatchPublisher(
      final PayloadEncoder payloadEncoder, final RecoveryClient resendClient) {
    this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    this.resendClient = Objects.requireNonNull(resendClient);
  }

  @Override
  public void publishBatch(final List<EncodedPayload> payloads, final String targetUrl) {

    LOGGER.info("Publishing message to {}", targetUrl);

    final List<byte[]> encodedPayloads =
        payloads.stream().map(payloadEncoder::encode).collect(Collectors.toList());

    final PushBatchRequest pushBatchRequest = new PushBatchRequest(encodedPayloads);

    final boolean result = resendClient.pushBatch(targetUrl, pushBatchRequest);

    if (!result) {
      throw new PublishPayloadException("Unable to push payload batch to recipient " + targetUrl);
    }

    LOGGER.info("Published to {}", targetUrl);
  }
}
