package com.quorum.tessera.grpc.p2p;

import com.quorum.tessera.client.GrpcP2pClient;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.partyinfo.PublishPayloadException;
import com.quorum.tessera.partyinfo.ResendBatchPublisher;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GrpcResendBatchPublisher implements ResendBatchPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcResendBatchPublisher.class);

    private final PayloadEncoder payloadEncoder;

    private final P2pClient p2pClient;

    public GrpcResendBatchPublisher() {
        this(PayloadEncoder.create(),new GrpcP2pClient());
    }

    public GrpcResendBatchPublisher(
            final PayloadEncoder payloadEncoder,
            final P2pClient p2pClient) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.p2pClient = Objects.requireNonNull(p2pClient);

    }

    @Override
    public void publishBatch(final List<EncodedPayload> payloads, final String targetUrl) {

        LOGGER.info("Publishing message to {}", targetUrl);

        final List<byte[]> encodedPayloads = payloads.stream().map(payloadEncoder::encode).collect(Collectors.toList());

        final boolean result = p2pClient.pushBatch(targetUrl, new com.quorum.tessera.partyinfo.PushBatchRequest(encodedPayloads));

        if (!result) {
            throw new PublishPayloadException(
                    "Unable to push payload batch to recipient " + targetUrl);
        }

        LOGGER.info("Published to {}", targetUrl);
    }
    
}
