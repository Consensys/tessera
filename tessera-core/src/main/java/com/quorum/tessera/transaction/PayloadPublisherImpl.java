
package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

 
public class PayloadPublisherImpl implements PayloadPublisher {
    
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

        if (!partyInfoService.getPartyInfo().getUrl().equals(targetUrl)) {

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
        }
    }


    
}
