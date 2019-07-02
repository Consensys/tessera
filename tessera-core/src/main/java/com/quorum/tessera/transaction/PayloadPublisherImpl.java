package com.quorum.tessera.transaction;

import com.quorum.tessera.partyinfo.P2pClient;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.partyinfo.PartyInfoService;
import com.quorum.tessera.transaction.exception.PublishPayloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class PayloadPublisherImpl implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadPublisherImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    private final Enclave enclave;

    public PayloadPublisherImpl(
            final PayloadEncoder payloadEncoder,
            final PartyInfoService partyInfoService,
            final P2pClient p2pClient,
            final Enclave enclave) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.p2pClient = Objects.requireNonNull(p2pClient);
        this.enclave = Objects.requireNonNull(enclave);
    }

    @Override
    public void publishPayload(final EncodedPayload payload, final PublicKey recipientKey) {

        if (enclave.getPublicKeys().contains(recipientKey)) {
            // we are trying to send something to ourselves - don't do it
            LOGGER.debug(
                    "Trying to send message to ourselves with key {}, not publishing", recipientKey.encodeToBase64());
            return;
        }

        final String targetUrl = partyInfoService.getURLFromRecipientKey(recipientKey);

        LOGGER.info("Publishing message to {}", targetUrl);

        final byte[] encoded = payloadEncoder.encode(payload);

        byte[] pushResponse = p2pClient.push(targetUrl, encoded);

        if (pushResponse == null) {
            throw new PublishPayloadException("Unable to push payload to recipient " + recipientKey.encodeToBase64());
        }

        LOGGER.info("Published to {}", targetUrl);
    }
}
