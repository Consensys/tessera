package com.quorum.tessera.transaction;

import com.quorum.tessera.client.P2pClient;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.node.PartyInfoService;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.transaction.Transactional;

@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadEncoder payloadEncoder;

    private final KeyManager keyManager;

    private final NaclFacade nacl;

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    public TransactionServiceImpl(final EncryptedTransactionDAO encryptedTransactionDAO,
            final PayloadEncoder payloadEncoder,
            final KeyManager keyManager,
            final NaclFacade nacl,
            PartyInfoService partyInfoService,
            P2pClient p2pClient) {
        
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.keyManager = Objects.requireNonNull(keyManager);
        this.nacl = Objects.requireNonNull(nacl);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.p2pClient = Objects.requireNonNull(p2pClient);
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
            final Nonce nonce = encodedPayload.getRecipientNonce();
            final byte[] masterKeyBytes = nacl.openAfterPrecomputation(recipientBox, nonce, sharedKey);

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

//****
    @Override
    public byte[] receive(final byte[] hashBytes, final Optional<byte[]> to) {
        final MessageHash hash = new MessageHash(hashBytes);

        if (to.isPresent()) {
            return retrieveUnencryptedTransaction(hash, new Key(to.get()));
        } else {
            for (final Key potentialMatchingKey : this.keyManager.getPublicKeys()) {
                try {
                    return retrieveUnencryptedTransaction(hash, potentialMatchingKey);
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

        EncodedPayloadWithRecipients encryptedPayload
                = encryptPayload(message, senderPublicKey, recipientList);

        MessageHash messageHash = storeEncodedPayload(encryptedPayload);

        recipientList.forEach(recipient -> publishPayload(encryptedPayload, recipient));

        return messageHash;

    }

    @Override
    public MessageHash storePayload(final byte[] payload) {
        return storeEncodedPayload(
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

    @Override
    public void resendAll(final byte[] recipientPublicKey) {
        final Key recipient = new Key(recipientPublicKey);

        final Collection<EncodedPayloadWithRecipients> payloads
                = retrieveAllForRecipient(recipient);

        payloads.forEach(payload
                -> payload.getRecipientKeys().forEach(recipientKey
                        -> publishPayload(payload, recipientKey)
                )
        );
    }

    @Override
    public EncodedPayloadWithRecipients fetchTransactionForRecipient(final MessageHash hash, final Key recipient) {
        final EncodedPayloadWithRecipients payloadWithRecipients = retrievePayload(hash);

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
