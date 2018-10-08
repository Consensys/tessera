package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.api.model.ResendResponse;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.key.KeyManager;
import com.quorum.tessera.key.KeyUtil;
import com.quorum.tessera.key.PrivateKey;
import com.quorum.tessera.key.PublicKey;
import com.quorum.tessera.nacl.Key;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.nacl.NaclFacade;
import com.quorum.tessera.nacl.Nonce;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncodedPayload;
import com.quorum.tessera.transaction.model.EncodedPayloadWithRecipients;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import com.quorum.tessera.util.Base64Decoder;
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
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegate/Mediator object to normalise calls/interactions between Enclave and
 * Base64Decoder
 *
 * @see {Base64Decoder}
 * @see {Enclave}
 */
@Transactional
public class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionManagerImpl.class);

    private final PayloadEncoder payloadEncoder;

    private final Base64Decoder base64Decoder;

    private final KeyManager keyManager;

    private final NaclFacade nacl;

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadPublisher payloadPublisher;

    public TransactionManagerImpl(
            Base64Decoder base64Decoder,
            PayloadEncoder payloadEncoder,
            KeyManager keyManager,
            NaclFacade nacl,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PayloadPublisher payloadPublisher) {

        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.keyManager = Objects.requireNonNull(keyManager);
        this.nacl = Objects.requireNonNull(nacl);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        final String sender = sendRequest.getFrom();

        final PublicKey senderPublicKey = Optional.ofNullable(sender)
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .orElseGet(() -> PublicKey.from(keyManager.defaultPublicKey().getKeyBytes()));

        final byte[][] recipients = Stream
                .of(sendRequest.getTo())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream
                .of(recipients)
                .map(PublicKey::from)
                .collect(Collectors.toList());


        recipientList.addAll(keyManager.getForwardingKeys());
        

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        EncodedPayloadWithRecipients encryptedPayload
                = encryptPayload(payload, senderPublicKey, recipientList);

        MessageHash messageHash = storeEncodedPayload(encryptedPayload);

        recipientList.forEach(recipient -> payloadPublisher.publishPayload(encryptedPayload, recipient));

        final byte[] key = messageHash.getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        return new SendResponse(encodedKey);
    }


    private EncodedPayloadWithRecipients encryptPayload(final byte[] message,
            final PublicKey senderPublicKey,
            final List<PublicKey> recipientPublicKeys) {

        final Key masterKey = nacl.createSingleKey();
        final Nonce nonce = nacl.randomNonce();
        final Nonce recipientNonce = nacl.randomNonce();

        final byte[] cipherText = nacl.sealAfterPrecomputation(message, nonce, masterKey);

        final PrivateKey privateKey = keyManager.getPrivateKeyForPublicKey(senderPublicKey);

        final Key privateKeyKey = new Key(privateKey.getKeyBytes());
        
        final List<byte[]> encryptedMasterKeys = recipientPublicKeys
                .stream()
                .map(PublicKey::getKeyBytes)
                .map(Key::new)
                .map(key -> nacl.computeSharedKey(key, privateKeyKey))
                .map(key -> nacl.sealAfterPrecomputation(masterKey.getKeyBytes(), recipientNonce, key))
                .collect(Collectors.toList());

        return new EncodedPayloadWithRecipients(
                new EncodedPayload(senderPublicKey, cipherText, nonce, encryptedMasterKeys, recipientNonce),
                recipientPublicKeys
        );

    }

    private MessageHash storeEncodedPayload(final EncodedPayloadWithRecipients payloadWithRecipients) {

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
    public ResendResponse resend(ResendRequest request) {

        final byte[] publicKey = base64Decoder.decode(request.getPublicKey());

        if (request.getType() == ResendRequestType.ALL) {
            resendAll(publicKey);
            return new ResendResponse();
        } else {
            final byte[] hashKey = base64Decoder.decode(request.getKey());

            final EncodedPayloadWithRecipients payloadWithRecipients = fetchTransactionForRecipient(new MessageHash(hashKey), PublicKey.from(publicKey));

            final byte[] encoded = payloadEncoder.encode(payloadWithRecipients);

            return new ResendResponse(encoded);
        }
    }

    @Override
    public void storePayload(byte[] payload) {

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = payloadEncoder.decodePayloadWithRecipients(payload);

        final MessageHash messageHash = storeEncodedPayload(encodedPayloadWithRecipients);

        LOGGER.info(base64Decoder.encodeToString(messageHash.getHashBytes()));
    }

    @Override
    public void delete(DeleteRequest request) {
        final byte[] hashBytes = base64Decoder.decode(request.getKey());
        final MessageHash messageHash = new MessageHash(hashBytes);

        LOGGER.info("Received request to delete message with hash {}", messageHash);
        this.encryptedTransactionDAO.delete(messageHash);

    }

    @Override
    public ReceiveResponse receive(ReceiveRequest request) {
        final byte[] key = base64Decoder.decode(request.getKey());

        final Optional<byte[]> to = Optional
                .ofNullable(request.getTo())
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final MessageHash hash = new MessageHash(key);

        
        if (to.isPresent()) {
            byte[] payload = retrieveUnencryptedTransaction(hash, PublicKey.from(to.get()));
            String encodedPayload = base64Decoder.encodeToString(payload);
            return new ReceiveResponse(encodedPayload);
        } else {
            for (final PublicKey potentialMatchingKey : this.keyManager.getPublicKeys()) {
                try {
                    byte[] payload = retrieveUnencryptedTransaction(hash, potentialMatchingKey);
                    String encodedPayload = base64Decoder.encodeToString(payload);
                    return new ReceiveResponse(encodedPayload);
                } catch (final NaclException ex) {
                    LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
                }
            }

            throw new RuntimeException("No key found that could decrypt the requested payload: " + hash.toString());
        }
    }

    private byte[] retrieveUnencryptedTransaction(final MessageHash hash, final PublicKey providedKey) {

        final EncryptedTransaction encryptedTransaction = encryptedTransactionDAO
                .retrieveByHash(hash)
                .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));

        final EncodedPayloadWithRecipients payloadWithRecipients
                = payloadEncoder.decodePayloadWithRecipients(encryptedTransaction.getEncodedPayload());

        final EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        final PublicKey senderPubKey;

        final PublicKey recipientPubKey;

        if (!keyManager.getPublicKeys().contains(encodedPayload.getSenderKey())) {
            // This is a payload originally sent to us by another node
            recipientPubKey = encodedPayload.getSenderKey();
            senderPubKey = providedKey;
        } else {
            // This is a payload that originated from us
            senderPubKey = encodedPayload.getSenderKey();
            recipientPubKey = payloadWithRecipients.getRecipientKeys().get(0);
        }

        final PrivateKey senderPrivKey = keyManager.getPrivateKeyForPublicKey(senderPubKey);
        Key senderPrivKeyKey = new Key(senderPrivKey.getKeyBytes());
        Key recipientPubKeyKey = new Key(recipientPubKey.getKeyBytes());
        
        final Key sharedKey = nacl.computeSharedKey(recipientPubKeyKey, senderPrivKeyKey);

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

    private void resendAll(final byte[] recipientPublicKey) {
        final PublicKey recipient = PublicKey.from(recipientPublicKey);

        final Collection<EncodedPayloadWithRecipients> payloads
                = retrieveAllForRecipient(recipient);

        payloads.forEach(payload
                -> payload.getRecipientKeys().forEach(recipientKey
                        -> payloadPublisher.publishPayload(payload, recipientKey)
                )
        );
    }

    private Collection<EncodedPayloadWithRecipients> retrieveAllForRecipient(final PublicKey recipientPublicKey) {
        LOGGER.debug("Retrieving all transaction for recipient {}", recipientPublicKey);

        return encryptedTransactionDAO
                .retrieveAllTransactions()
                .stream()
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decodePayloadWithRecipients)
                .filter(payload -> payload.getRecipientKeys().contains(recipientPublicKey))
                .collect(toList());
    }

    private EncodedPayloadWithRecipients fetchTransactionForRecipient(final MessageHash hash, final PublicKey recipient) {
        final EncodedPayloadWithRecipients payloadWithRecipients = encryptedTransactionDAO
                .retrieveByHash(hash)
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decodePayloadWithRecipients)
                .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));

        final EncodedPayload encodedPayload = payloadWithRecipients.getEncodedPayload();

        if (!payloadWithRecipients.getRecipientKeys().contains(recipient)) {
            
            throw new RuntimeException("Recipient " + KeyUtil.encodeToBase64(recipient) + " is not a recipient of transaction " + hash);
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
