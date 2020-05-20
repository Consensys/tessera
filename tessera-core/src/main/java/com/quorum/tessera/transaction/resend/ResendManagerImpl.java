package com.quorum.tessera.transaction.resend;

import com.quorum.tessera.data.EncryptedTransaction;
import com.quorum.tessera.data.EncryptedTransactionDAO;
import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.data.MessageHashFactory;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.exception.PrivacyViolationException;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ResendManagerImpl implements ResendManager {

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadEncoder payloadEncoder;

    private final Enclave enclave;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    public ResendManagerImpl(EncryptedTransactionDAO encryptedTransactionDAO, Enclave enclave) {
        this(encryptedTransactionDAO, PayloadEncoder.create(), enclave);
    }

    public ResendManagerImpl(final EncryptedTransactionDAO dao, final PayloadEncoder encoder, final Enclave enclave) {
        this.encryptedTransactionDAO = dao;
        this.payloadEncoder = encoder;
        this.enclave = enclave;
    }

    // TODO: synchronize based on messagehash, so different message don't lock each other
    @Transactional
    public synchronized void acceptOwnMessage(final byte[] message) {

        final EncodedPayload payload = payloadEncoder.decode(message);

        // check the payload can be decrpyted to ensure it isn't rubbish being sent to us
        final byte[] newDecrypted = enclave.unencryptTransaction(payload, null);

        final MessageHash transactionHash =
                Optional.of(payload)
                        .map(EncodedPayload::getCipherText)
                        .map(messageHashFactory::createFromCipherText)
                        .get();

        final PublicKey sender = payload.getSenderKey();
        if (!enclave.getPublicKeys().contains(sender)) {
            throw new IllegalArgumentException(
                    "Message " + transactionHash.toString() + " does not have one the nodes own keys as a sender");
        }

        // this is a tx which we created
        final Optional<EncryptedTransaction> tx = this.encryptedTransactionDAO.retrieveByHash(transactionHash);

        if (tx.isPresent()) {

            // we just need to add the recipient
            final byte[] encodedPayload = tx.get().getEncodedPayload();
            final EncodedPayload existing = payloadEncoder.decode(encodedPayload);
            final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.from(existing);

            // lets compare it against the previous version of the message
            final byte[] oldDecrypted = enclave.unencryptTransaction(existing, null);
            final boolean same =
                    Arrays.equals(newDecrypted, oldDecrypted)
                            && Arrays.equals(payload.getCipherText(), existing.getCipherText())
                            && (payload.getPrivacyMode() == existing.getPrivacyMode());

            if (!same) {
                throw new IllegalArgumentException("Invalid payload provided");
            }

            // check recipients
            if (existing.getPrivacyMode() == PrivacyMode.PRIVATE_STATE_VALIDATION) {
                if (!existing.getRecipientKeys().containsAll(payload.getRecipientKeys())
                        || !payload.getRecipientKeys().containsAll(existing.getRecipientKeys())) {
                    throw new PrivacyViolationException(
                            "Participants mismatch for two versions of transaction " + transactionHash);
                }
            } else if (!existing.getRecipientKeys().contains(payload.getRecipientKeys().get(0))) {
                payloadBuilder
                        .withRecipientKey(payload.getRecipientKeys().get(0))
                        .withRecipientBox(payload.getRecipientBoxes().get(0).getData());
            }

            // add any ACOTHs that other parties may have missed
            payloadBuilder.withAffectedContractTransactions(
                    payload.getAffectedContractTransactions().entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getData())));

            EncryptedTransaction encryptedTransaction = tx.get();

            encryptedTransaction.setEncodedPayload(payloadEncoder.encode(payloadBuilder.build()));

            this.encryptedTransactionDAO.update(encryptedTransaction);

        } else {

            final EncodedPayload.Builder payloadBuilder = EncodedPayload.Builder.from(payload);
            final List<PublicKey> recipientKeys = new ArrayList<>(payload.getRecipientKeys());

            // we need to recreate this
            if (!recipientKeys.contains(sender)) {
                recipientKeys.add(sender);
                payloadBuilder.withRecipientKey(sender);
            }

            // add recipient boxes for all recipients (for PSV transactions)
            IntStream.range(payload.getRecipientBoxes().size(), recipientKeys.size())
                    .forEach(
                            i -> {
                                PublicKey recipient = recipientKeys.get(i);
                                byte[] newBox = enclave.createNewRecipientBox(payload, recipient);
                                payloadBuilder.withRecipientBox(newBox);
                            });

            final byte[] encoded = payloadEncoder.encode(payloadBuilder.build());

            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, encoded));
        }
    }
}
