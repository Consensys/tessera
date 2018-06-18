package com.github.nexus.enclave;

import com.github.nexus.api.model.ApiPath;
import com.github.nexus.enclave.model.MessageHash;
import com.github.nexus.nacl.Key;
import com.github.nexus.node.PartyInfoService;
import com.github.nexus.node.PostDelegate;
import com.github.nexus.transaction.PayloadEncoder;
import com.github.nexus.transaction.TransactionService;
import com.github.nexus.transaction.model.EncodedPayload;
import com.github.nexus.transaction.model.EncodedPayloadWithRecipients;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private final TransactionService transactionService;

    private final PartyInfoService partyInfoService;

    private final PayloadEncoder payloadEncoder;

    private final PostDelegate postDelegate;

    public EnclaveImpl(final TransactionService transactionService,
                             PartyInfoService partyInfoService,
                             PayloadEncoder payloadEncoder,
                             PostDelegate postDelegate) {
            this.transactionService = requireNonNull(transactionService,"transactionService cannot be null");
            this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService cannot be null");
            this.payloadEncoder = requireNonNull(payloadEncoder,"encoder cannot be null");
            this.postDelegate = requireNonNull(postDelegate, "postDelegate cannot be null");

    }

    @Override
    public boolean delete(final byte[] hashBytes) {
        MessageHash messageHash = new MessageHash(hashBytes);
        return transactionService.delete(messageHash);
    }

    @Override
    public byte[] receive(final byte[] key, final byte[] to) {
        return transactionService.retrieveUnencryptedTransaction(new MessageHash(key), new Key(to));
    }

    @Override
    public MessageHash store(final byte[] sender, final byte[][] recipients, final byte[] message) {
        Key senderPublicKey = new Key(sender);
        List<Key> recipientList = Arrays.stream(recipients)
            .map(recipient -> new Key(recipient))
            .collect(Collectors.toList());

        EncodedPayloadWithRecipients encryptedPayload =
            transactionService.encryptPayload(message, senderPublicKey, recipientList);

        MessageHash messageHash = transactionService.storeEncodedPayload(encryptedPayload);

        recipientList.forEach(recipient -> {
            publishPayload(encryptedPayload, recipient);
        });

        return messageHash;

    }

    @Override
    public MessageHash storePayload(final byte[] payload) {
        EncodedPayloadWithRecipients encodedPayloadWithRecipients =
            payloadEncoder.decodePayloadWithRecipients(payload);

        return transactionService.storeEncodedPayload(encodedPayloadWithRecipients);
    }

    @Override
    public void publishPayload(final EncodedPayloadWithRecipients encodedPayloadWithRecipients,
                               final Key recipientKey) {

        String url = partyInfoService.getURLFromRecipientKey(recipientKey);

        if (!partyInfoService.getPartyInfo().getUrl().equals(url)){

            EncodedPayload encodedPayload = encodedPayloadWithRecipients.getEncodedPayload();

            int index = encodedPayloadWithRecipients.getRecipientKeys().indexOf(recipientKey);

            EncodedPayloadWithRecipients encodedPayloadWithOneRecipient =
                new EncodedPayloadWithRecipients(
                    new EncodedPayload(encodedPayload.getSenderKey(),
                        encodedPayload.getCipherText(),
                        encodedPayload.getCipherTextNonce(),
                        Arrays.asList(encodedPayload.getRecipientBoxes().get(index)),
                        encodedPayload.getRecipientNonce()),
                    Collections.emptyList());

            byte[] encoded = payloadEncoder.encode(encodedPayloadWithOneRecipient);

            postDelegate.doPost(url, ApiPath.PUSH, encoded);
        }
    }

    @Override
    public void resendAll(byte[] recipientPublicKey) {
        Key recipient = new Key(recipientPublicKey);
        Collection<EncodedPayloadWithRecipients> payloads = transactionService.retrieveAllForRecipient(recipient);

        payloads.forEach(payload -> {
            payload.getRecipientKeys().forEach(recipientKey -> {
                publishPayload(payload, recipientKey);
            });
        });
    }
}
