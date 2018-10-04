package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.DeleteRequest;
import com.quorum.tessera.api.model.ReceiveRequest;
import com.quorum.tessera.api.model.ReceiveResponse;
import com.quorum.tessera.api.model.ResendRequest;
import com.quorum.tessera.api.model.ResendRequestType;
import com.quorum.tessera.api.model.SendRequest;
import com.quorum.tessera.api.model.SendResponse;
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
import com.quorum.tessera.util.Base64Decoder;
import java.util.Collection;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.joining;
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

    private final PartyInfoService partyInfoService;

    private final P2pClient p2pClient;

    public TransactionManagerImpl(
            Base64Decoder base64Decoder,
            PayloadEncoder payloadEncoder,
            KeyManager keyManager,
            NaclFacade nacl,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PartyInfoService partyInfoService,
            P2pClient p2pClient) {

        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.keyManager = Objects.requireNonNull(keyManager);
        this.nacl = Objects.requireNonNull(nacl);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.partyInfoService = Objects.requireNonNull(partyInfoService);
        this.p2pClient = Objects.requireNonNull(p2pClient);
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        LOGGER.debug("Received send request");

        final String sender = sendRequest.getFrom();
        LOGGER.debug("Received send request from {}", sender);
        final Optional<byte[]> from = Optional.ofNullable(sender)
                .map(base64Decoder::decode);

        LOGGER.debug("SEND: sender {}", sender);

        final byte[][] recipients = Stream
                .of(sendRequest.getTo())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SEND: recipients {}", Stream.of(sendRequest.getTo()).collect(joining()));

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        final MessageHash messageHash = store(from, recipients, payload);

        final byte[] key = messageHash.getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);
        return new SendResponse(encodedKey);
    }

    private MessageHash store(final Optional<byte[]> sender, final byte[][] recipients, final byte[] message) {

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

    private EncodedPayloadWithRecipients encryptPayload(final byte[] message,
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
    public String storeAndEncodeKey(String sender, String recipientKeys, byte[] payload) {
        final Optional<byte[]> from = Optional
                .ofNullable(sender)
                .map(base64Decoder::decode);

        final String nonnullRecipients = Optional.ofNullable(recipientKeys).orElse("");
        final byte[][] recipients = Stream.of(nonnullRecipients.split(","))
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        LOGGER.debug("SendRaw Recipients: {}", nonnullRecipients);

        final MessageHash messageHash = store(from, recipients, payload);
        final byte[] key = messageHash.getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        LOGGER.debug("Encoded key: {}", encodedKey);

        return encodedKey;
    }

    @Override
    public String receiveAndEncode(ReceiveRequest request) {

        final byte[] key = base64Decoder.decode(request.getKey());

        final Optional<byte[]> to = Optional
                .ofNullable(request.getTo())
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final byte[] payload = receive(key, to);

        return base64Decoder.encodeToString(payload);
    }

    @Override
    public Optional<byte[]> resendAndEncode(ResendRequest request) {

        final byte[] publicKey = base64Decoder.decode(request.getPublicKey());

        if (request.getType() == ResendRequestType.ALL) {
            resendAll(publicKey);
            return Optional.empty();
        } else {
            final byte[] hashKey = base64Decoder.decode(request.getKey());

            final EncodedPayloadWithRecipients payloadWithRecipients = fetchTransactionForRecipient(new MessageHash(hashKey), new Key(publicKey));

            final byte[] encoded = payloadEncoder.encode(payloadWithRecipients);

            return Optional.of(encoded);
        }
    }

    @Override
    public void storePayload(byte[] payload) {
        final MessageHash messageHash = storeEncodedPayload(
                payloadEncoder.decodePayloadWithRecipients(payload)
        );
        LOGGER.info(base64Decoder.encodeToString(messageHash.getHashBytes()));
    }

    @Override
    public void delete(DeleteRequest request) {
        final byte[] hashBytes = base64Decoder.decode(request.getKey());
        final MessageHash messageHash = new MessageHash(hashBytes);

        delete(messageHash);

    }

    @Override
    public ReceiveResponse receive(String hash, String toStr) {

        final byte[] key = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(toStr)
                .filter(str -> !str.isEmpty())
                .map(base64Decoder::decode);

        final byte[] payload = receive(key, to);

        final String encodedPayload = base64Decoder.encodeToString(payload);

        return new ReceiveResponse(encodedPayload);
    }

    @Override
    public byte[] receiveRaw(String hash, String recipientKey) {

        final byte[] decodedKey = base64Decoder.decode(hash);

        final Optional<byte[]> to = Optional
                .ofNullable(recipientKey)
                .map(base64Decoder::decode);

        return receive(decodedKey, to);
    }

    @Override
    public void deleteKey(String key) {
        final byte[] hashBytes = base64Decoder.decode(key);
        MessageHash messageHash = new MessageHash(hashBytes);
        delete(messageHash);
    }

    public ReceiveResponse receive(ReceiveRequest request) {
        return receive(request.getKey(), request.getTo());
    }

    private byte[] receive(final byte[] hashBytes, final Optional<byte[]> to) {
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

    private byte[] retrieveUnencryptedTransaction(final MessageHash hash, final Key providedKey) {

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

    private void delete(final MessageHash hash) {
        LOGGER.info("Received request to delete message with hash {}", hash);
        this.encryptedTransactionDAO.delete(hash);
    }

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

    private void resendAll(final byte[] recipientPublicKey) {
        final Key recipient = new Key(recipientPublicKey);

        final Collection<EncodedPayloadWithRecipients> payloads
                = retrieveAllForRecipient(recipient);

        payloads.forEach(payload
                -> payload.getRecipientKeys().forEach(recipientKey
                        -> publishPayload(payload, recipientKey)
                )
        );
    }

    private Collection<EncodedPayloadWithRecipients> retrieveAllForRecipient(final Key recipientPublicKey) {
        LOGGER.debug("Retrieving all transaction for recipient {}", recipientPublicKey);

        return encryptedTransactionDAO
                .retrieveAllTransactions()
                .stream()
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decodePayloadWithRecipients)
                .filter(payload -> payload.getRecipientKeys().contains(recipientPublicKey))
                .collect(toList());
    }

    private EncodedPayloadWithRecipients fetchTransactionForRecipient(final MessageHash hash, final Key recipient) {
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

    private EncodedPayloadWithRecipients retrievePayload(final MessageHash hash) {
        return encryptedTransactionDAO
                .retrieveByHash(hash)
                .map(EncryptedTransaction::getEncodedPayload)
                .map(payloadEncoder::decodePayloadWithRecipients)
                .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));
    }
    

}
