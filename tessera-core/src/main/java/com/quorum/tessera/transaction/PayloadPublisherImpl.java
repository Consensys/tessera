package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoService;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PayloadPublisherImpl implements PayloadPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(PayloadPublisherImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    public PayloadPublisherImpl(PayloadEncoder payloadEncoder,
            PartyInfoService partyInfoService, P2pClient p2pClient) {
        this.payloadEncoder = payloadEncoder;
        this.partyInfoService = partyInfoService;
        this.p2pClient = p2pClient;
    }

    @Override
    public void publishPayload(EncodedPayloadWithRecipients encodedPayloadWithRecipients, PublicKey recipientKey) {
        final String targetUrl = partyInfoService.getURLFromRecipientKey(recipientKey);
        
        final String url = partyInfoService.getPartyInfo().getUrl();
        if (Objects.equals(url,targetUrl)) {
            LOGGER.debug("Own url {} is same as target {}. Not publishing", url,targetUrl);
            return;
        }

        final EncodedPayload encodedPayload = encodedPayloadWithRecipients.getEncodedPayload();

        final int index = encodedPayloadWithRecipients.getRecipientKeys().indexOf(recipientKey);

        final EncodedPayloadWithRecipients encodedPayloadWithOneRecipient
                = new EncodedPayloadWithRecipients(
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
