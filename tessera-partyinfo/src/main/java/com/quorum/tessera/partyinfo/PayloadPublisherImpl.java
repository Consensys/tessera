package com.quorum.tessera.partyinfo;

import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PayloadPublisherImpl implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadPublisherImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final P2pClient p2pClient;

    public PayloadPublisherImpl(final P2pClient p2pClient) {
        this(PayloadEncoder.create(), p2pClient);
    }

    public PayloadPublisherImpl(final PayloadEncoder payloadEncoder, final P2pClient p2pClient) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder, "payloadEncoder is required");
        this.p2pClient = Objects.requireNonNull(p2pClient, "p2pClient is required");
    }

    @Override
    public void publishPayload(final EncodedPayload payload, final String targetUrl) {

        LOGGER.info("Publishing message to {}", targetUrl);

        final byte[] encoded = payloadEncoder.encode(payload);

        byte[] pushResponse = p2pClient.push(targetUrl, encoded);

        if (pushResponse == null) {
            throw new PublishPayloadException("Unable to push payload to recipient url " + targetUrl);
        }

        LOGGER.info("Published to {}", targetUrl);
    }
}
