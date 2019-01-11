package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PayloadEncoder;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

public class PayloadPublisherImpl implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadPublisherImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    private final Enclave enclave;

    public PayloadPublisherImpl(final PayloadEncoder payloadEncoder,
                                final PartyInfoService partyInfoService,
                                final P2pClient p2pClient,
                                final Enclave enclave) {
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.p2pClient = Objects.requireNonNull(p2pClient);
        this.enclave = Objects.requireNonNull(enclave);
    }

    @Override
    public void publishPayload(final EncodedPayloadWithRecipients encodedPayloadWithRecipients,
                               final PublicKey recipientKey) {

        if(enclave.getPublicKeys().contains(recipientKey)) {
            //we are trying to send something to ourselves - don't do it
            LOGGER.debug("Trying to send message to ourselves with key {}, not publishing", recipientKey.encodeToBase64());
            return;
        }

        final String targetUrl = partyInfoService.getURLFromRecipientKey(recipientKey);

        LOGGER.info("Publishing message to {}", targetUrl);

        final EncodedPayload encodedPayload = encodedPayloadWithRecipients.getEncodedPayload();

        final int index = encodedPayloadWithRecipients.getRecipientKeys().indexOf(recipientKey);

        final EncodedPayloadWithRecipients encodedPayloadWithOneRecipient = new EncodedPayloadWithRecipients(
            new EncodedPayload(
                encodedPayload.getSenderKey(),
                encodedPayload.getCipherText(),
                encodedPayload.getCipherTextNonce(),
                singletonList(encodedPayload.getRecipientBoxes().get(index)),
                encodedPayload.getRecipientNonce()
            ),
            emptyList()
        );

        final byte[] encoded = payloadEncoder.encode(encodedPayloadWithOneRecipient);
        p2pClient.push(targetUrl, encoded);
        LOGGER.info("Published to {}", targetUrl);

    }

}
