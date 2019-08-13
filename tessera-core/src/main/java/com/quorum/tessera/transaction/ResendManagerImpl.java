package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.PrivacyMode;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.data.*;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;

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
                    throw new IllegalArgumentException(
                            "Participants mismatch for two versions of transaction " + transactionHash);
                }
            } else if (!existing.getRecipientKeys().contains(payload.getRecipientKeys().get(0))) {
                existing.getRecipientKeys().add(payload.getRecipientKeys().get(0));
                existing.getRecipientBoxes().add(payload.getRecipientBoxes().get(0));
            }

            // add any ACOTHs that other parties may have missed
            existing.getAffectedContractTransactions().putAll(payload.getAffectedContractTransactions());

            EncryptedTransaction encryptedTransaction = tx.get();

            encryptedTransaction.setEncodedPayload(payloadEncoder.encode(existing));

            this.encryptedTransactionDAO.save(encryptedTransaction);

        } else {

            // we need to recreate this
            if (!payload.getRecipientKeys().contains(sender)) {
                payload.getRecipientKeys().add(sender);
            }
            // add recipient boxes for all recipients (for PSV transactions)
            for (int idx = payload.getRecipientBoxes().size(); idx < payload.getRecipientKeys().size(); idx++) {
                PublicKey recipient = payload.getRecipientKeys().get(idx);
                byte[] newbox = enclave.createNewRecipientBox(payload, recipient);
                payload.getRecipientBoxes().add(newbox);
            }

            final byte[] encoded = payloadEncoder.encode(payload);

            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, encoded));
        }
    }
}
