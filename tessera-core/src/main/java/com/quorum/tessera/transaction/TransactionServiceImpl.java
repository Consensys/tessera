package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadEncoder payloadEncoder;

    private final KeyManager keyManager;

    private final NaclFacade nacl;

    public TransactionServiceImpl(final EncryptedTransactionDAO encryptedTransactionDAO,
                                  final PayloadEncoder payloadEncoder,
                                  final KeyManager keyManager,
                                  final NaclFacade nacl) {
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.keyManager = Objects.requireNonNull(keyManager);
        this.nacl = Objects.requireNonNull(nacl);
    }

    @Override
    public void delete(final MessageHash hash) {
        LOGGER.info("Received request to delete message with hash {}", hash);
        this.encryptedTransactionDAO.delete(hash);
    }

    @Override
    public Collection<EncodedPayloadWithRecipients> retrieveAllForRecipient(final Key recipientPublicKey) {
        LOGGER.debug("Retrieving all transaction for recipient {}", recipientPublicKey);

        return encryptedTransactionDAO
            .retrieveAllTransactions()
            .stream()
            .map(EncryptedTransaction::getEncodedPayload)
            .map(payloadEncoder::decodePayloadWithRecipients)
            .filter(payload -> payload.getRecipientKeys().contains(recipientPublicKey))
            .collect(toList());
    }

    @Override
    public EncodedPayloadWithRecipients retrievePayload(final MessageHash hash) {
        return encryptedTransactionDAO
            .retrieveByHash(hash)
            .map(EncryptedTransaction::getEncodedPayload)
            .map(payloadEncoder::decodePayloadWithRecipients)
            .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));
    }

    @Override
    public byte[] retrieveUnencryptedTransaction(final MessageHash hash, final Key providedKey) {

        final EncryptedTransaction encryptedTransaction = encryptedTransactionDAO
            .retrieveByHash(hash)
            .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));

        final EncodedPayloadWithRecipients payloadWithRecipients
            = payloadEncoder.decodePayloadWithRecipients(encryptedTransaction.getEncodedPayload());

        final EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        final Key senderPubKey;
       
        final Key recipientPubKey;

        if (!keyManager.getPublicKeys().contains(encodedPayload.getSenderKey())) {
            // This is a payload originally sent to us by another node
            recipientPubKey = encodedPayload.getSenderKey();
            senderPubKey = providedKey;
        } else {
            // This is a payload that originated from us
            senderPubKey = encodedPayload.getSenderKey();
            recipientPubKey = payloadWithRecipients.getRecipientKeys().get(0);
        }

        final Key senderPrivKey = keyManager.getPrivateKeyForPublicKey(senderPubKey);
        final Key sharedKey = nacl.computeSharedKey(recipientPubKey, senderPrivKey);

        try {
            final byte[] recipientBox = encodedPayload.getRecipientBoxes().iterator().next();
            final Nonce nonce =  encodedPayload.getRecipientNonce();
            final byte[] masterKeyBytes = nacl.openAfterPrecomputation(recipientBox,nonce, sharedKey);

            final Key masterKey = new Key(masterKeyBytes);

            final byte[] cipherText = encodedPayload.getCipherText();
            final Nonce cipherTextNonce = encodedPayload.getCipherTextNonce();

            return nacl.openAfterPrecomputation(cipherText, cipherTextNonce, masterKey);

        } catch (final RuntimeException ex) {
            LOGGER.info("Couldn't decrypt message with hash {}. Our public key is: {}", hash, senderPubKey);
            LOGGER.debug("RuntimeException: ", ex);
            throw ex;
        }

    }

    @Override
    public MessageHash storeEncodedPayload(final EncodedPayloadWithRecipients payloadWithRecipients) {

        final SHA3.DigestSHA3 digestSHA3 = new SHA3.Digest512();
        final byte[] digest = digestSHA3.digest(payloadWithRecipients.getEncodedPayload().getCipherText());

        final MessageHash transactionHash = new MessageHash(digest);

        LOGGER.info("Generated transaction hash {}", transactionHash);

        final EncryptedTransaction newTransaction = new EncryptedTransaction(
            transactionHash,
            this.payloadEncoder.encode(payloadWithRecipients)
        );

        this.encryptedTransactionDAO.save(newTransaction);

        return transactionHash;
    }

    @Override
    public EncodedPayloadWithRecipients encryptPayload(final byte[] message,
                                                       final Key senderPublicKey,
                                                       final List<Key> recipientPublicKeys) {

        final Key masterKey = nacl.createSingleKey();
        final Nonce nonce = nacl.randomNonce();
        final Nonce recipientNonce = nacl.randomNonce();

        final byte[] cipherText = nacl.sealAfterPrecomputation(message, nonce, masterKey);

        final Key privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

        final List<byte[]> encryptedMasterKeys = recipientPublicKeys
            .stream()
            .map(key -> nacl.computeSharedKey(key, privateKey))
            .map(key -> nacl.sealAfterPrecomputation(masterKey.getKeyBytes(), recipientNonce, key))
            .collect(Collectors.toList());

        return new EncodedPayloadWithRecipients(
            new EncodedPayload(senderPublicKey, cipherText, nonce, encryptedMasterKeys, recipientNonce),
            recipientPublicKeys
        );

    }

}
