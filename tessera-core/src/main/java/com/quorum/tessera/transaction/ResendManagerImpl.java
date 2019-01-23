package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EncodedPayload;
import com.quorum.tessera.enclave.PayloadEncoder;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.enclave.model.MessageHashFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.model.EncryptedTransaction;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.Optional;

public class ResendManagerImpl implements ResendManager {

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadEncoder payloadEncoder;

    private final Enclave enclave;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    public ResendManagerImpl(final EncryptedTransactionDAO dao, final PayloadEncoder encoder, final Enclave enclave) {
        this.encryptedTransactionDAO = dao;
        this.payloadEncoder = encoder;
        this.enclave = enclave;
    }

    //TODO: synchronize based on messagehash, so different message don't lock each other
    @Transactional
    public synchronized void acceptOwnMessage(final byte[] message) {

        final EncodedPayload payload = payloadEncoder.decode(message);

        //check the payload can be decrpyted to ensure it isn't rubbish being sent to us
        final byte[] newDecrypted = enclave.unencryptTransaction(payload, null);

        final MessageHash transactionHash = Optional.of(payload)
            .map(EncodedPayload::getCipherText)
            .map(messageHashFactory::createFromCipherText)
            .get();

        final PublicKey sender = payload.getSenderKey();
        if (!enclave.getPublicKeys().contains(sender)) {
            throw new IllegalArgumentException(
                "Message " + transactionHash.toString() + " does not have one the nodes own keys as a sender"
            );
        }

        //this is a tx which we created
        final Optional<EncryptedTransaction> tx = this.encryptedTransactionDAO.retrieveByHash(transactionHash);

        if (tx.isPresent()) {

            //we just need to add the recipient
            final byte[] encodedPayload = tx.get().getEncodedPayload();
            final EncodedPayload existing = payloadEncoder.decode(encodedPayload);

            if (!existing.getRecipientKeys().contains(payload.getRecipientKeys().get(0))) {
                //lets compare it against another message received before
                final byte[] oldDecrypted = enclave.unencryptTransaction(existing, null);
                final boolean same = Arrays.equals(newDecrypted, oldDecrypted)
                    && Arrays.equals(payload.getCipherText(), existing.getCipherText());

                if (!same) {
                    throw new IllegalArgumentException("Invalid payload provided");
                }

                existing.getRecipientKeys().add(payload.getRecipientKeys().get(0));
                existing.getRecipientBoxes().add(payload.getRecipientBoxes().get(0));

                tx.get().setEncodedPayload(payloadEncoder.encode(existing));

                this.encryptedTransactionDAO.save(tx.get());
            }

        } else {

            //we need to recreate this
            payload.getRecipientKeys().add(sender);
            byte[] newbox = enclave.createNewRecipientBox(payload, sender);
            payload.getRecipientBoxes().add(newbox);

            final byte[] encoded = payloadEncoder.encode(payload);

            this.encryptedTransactionDAO.save(new EncryptedTransaction(transactionHash, encoded));

        }

    }

}
