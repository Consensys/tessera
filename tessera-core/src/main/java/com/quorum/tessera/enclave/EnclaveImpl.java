package com.quorum.tessera.enclave;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.transaction.PayloadEncoder;
import com.quorum.tessera.transaction.TransactionService;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnclaveImpl.class);

    private final TransactionService transactionService;

    private final PartyInfoService partyInfoService;

    private final PayloadEncoder payloadEncoder;

    private final KeyManager keyManager;

    private final P2pClient p2pClient;
    
    public EnclaveImpl(final TransactionService transactionService,
                       final PartyInfoService partyInfoService,
                       final PayloadEncoder payloadEncoder,
                       final KeyManager keyManager,
                       final P2pClient p2pClient) {
        this.transactionService = requireNonNull(transactionService, "transactionService cannot be null");
        this.partyInfoService = requireNonNull(partyInfoService, "partyInfoService cannot be null");
        this.payloadEncoder = requireNonNull(payloadEncoder, "encoder cannot be null");
        this.keyManager = requireNonNull(keyManager, "keyManager cannot be null");
        this.p2pClient = requireNonNull(p2pClient);
    }

    @Override
    public void delete(final byte[] hashBytes) {
        final MessageHash messageHash = new MessageHash(hashBytes);

        this.transactionService.delete(messageHash);
    }

    @Override
    public byte[] receive(final byte[] hashBytes, final Optional<byte[]> to) {
        final MessageHash hash = new MessageHash(hashBytes);

        if (to.isPresent()) {
            return transactionService.retrieveUnencryptedTransaction(hash, new Key(to.get()));
        } else {
            for (final Key potentialMatchingKey : this.keyManager.getPublicKeys()) {
                try {
                    return transactionService.retrieveUnencryptedTransaction(hash, potentialMatchingKey);
                } catch (final NaclException ex) {
                    LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
                }
            }

            throw new RuntimeException("No key found that could decrypt the requested payload: " + hash.toString());
        }
    }

    @Override
    public MessageHash store(final Optional<byte[]> sender, final byte[][] recipients, final byte[] message) {

        final Key senderPublicKey = sender
            .map(Key::new)
            .orElseGet(keyManager::defaultPublicKey);

        final List<Key> recipientList = Stream
            .of(recipients)
            .map(Key::new)
            .collect(Collectors.toList());

        recipientList.addAll(keyManager.getForwardingKeys());

        EncodedPayloadWithRecipients encryptedPayload =
            transactionService.encryptPayload(message, senderPublicKey, recipientList);

        MessageHash messageHash = transactionService.storeEncodedPayload(encryptedPayload);

        recipientList.forEach(recipient -> publishPayload(encryptedPayload, recipient));

        return messageHash;

    }

    @Override
    public MessageHash storePayload(final byte[] payload) {
        return transactionService.storeEncodedPayload(
            payloadEncoder.decodePayloadWithRecipients(payload)
        );
    }

    @Override
    public void publishPayload(final EncodedPayloadWithRecipients encodedPayloadWithRecipients,
                               final Key recipientKey) {

        final String targetUrl = partyInfoService.getURLFromRecipientKey(recipientKey);

        if (!partyInfoService.getPartyInfo().getUrl().equals(targetUrl)) {

            final EncodedPayload encodedPayload = encodedPayloadWithRecipients.getEncodedPayload();

            final int index = encodedPayloadWithRecipients.getRecipientKeys().indexOf(recipientKey);

            final EncodedPayloadWithRecipients encodedPayloadWithOneRecipient =
                new EncodedPayloadWithRecipients(
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

    @Override
    public void resendAll(final byte[] recipientPublicKey) {
        final Key recipient = new Key(recipientPublicKey);

        final Collection<EncodedPayloadWithRecipients> payloads
            = this.transactionService.retrieveAllForRecipient(recipient);

        payloads.forEach(payload ->
            payload.getRecipientKeys().forEach(recipientKey ->
                publishPayload(payload, recipientKey)
            )
        );
    }

    @Override
    public EncodedPayloadWithRecipients fetchTransactionForRecipient(final MessageHash hash, final Key recipient) {
        final EncodedPayloadWithRecipients payloadWithRecipients = transactionService.retrievePayload(hash);

        final EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        if (!payloadWithRecipients.getRecipientKeys().contains(recipient)) {
            throw new RuntimeException("Recipient " + recipient + " is not a recipient of transaction " + hash);
        }

        final int recipientIndex = payloadWithRecipients.getRecipientKeys().indexOf(recipient);
        final byte[] recipientBox = encodedPayload.getRecipientBoxes().get(recipientIndex);

        return new EncodedPayloadWithRecipients(
            new EncodedPayload(
                encodedPayload.getSenderKey(),
                encodedPayload.getCipherText(),
                encodedPayload.getCipherTextNonce(),
                singletonList(recipientBox),
                encodedPayload.getRecipientNonce()
            ),
            emptyList()
        );

    }
}
