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
import com.quorum.tessera.enclave.model.MessageHashFactory;
import com.quorum.tessera.encryption.Enclave;
import com.quorum.tessera.encryption.EncodedPayload;
import com.quorum.tessera.encryption.EncodedPayloadWithRecipients;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.nacl.NaclException;
import com.quorum.tessera.transaction.exception.TransactionNotFoundException;
import com.quorum.tessera.transaction.model.EncryptedTransaction;
import com.quorum.tessera.util.Base64Decoder;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;
import javax.transaction.Transactional;
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

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    private final PayloadPublisher payloadPublisher;

    private final Enclave enclave;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    public TransactionManagerImpl(
            Base64Decoder base64Decoder,
            PayloadEncoder payloadEncoder,
            EncryptedTransactionDAO encryptedTransactionDAO,
            PayloadPublisher payloadPublisher,
            Enclave enclave) {

        this.base64Decoder = Objects.requireNonNull(base64Decoder);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.payloadPublisher = Objects.requireNonNull(payloadPublisher);
        this.enclave = Objects.requireNonNull(enclave);
    }

    @Override
    public SendResponse send(SendRequest sendRequest) {

        final String sender = sendRequest.getFrom();

        final PublicKey senderPublicKey = Optional.ofNullable(sender)
                .map(base64Decoder::decode)
                .map(PublicKey::from)
                .orElseGet(enclave::defaultPublicKey);

        final byte[][] recipients = Stream
                .of(sendRequest.getTo())
                .map(base64Decoder::decode)
                .toArray(byte[][]::new);

        final List<PublicKey> recipientList = Stream
                .of(recipients)
                .map(PublicKey::from)
                .collect(Collectors.toList());

        recipientList.addAll(enclave.getForwardingKeys());

        final byte[] payload = base64Decoder.decode(sendRequest.getPayload());

        EncodedPayloadWithRecipients encodedPayloadWithRecipients
                = enclave.encryptPayload(payload, senderPublicKey, recipientList);

        final MessageHash transactionHash = Optional.of(encodedPayloadWithRecipients)
                .map(EncodedPayloadWithRecipients::getEncodedPayload)
                .map(EncodedPayload::getCipherText)
                .map(messageHashFactory::createFromCipherText).get();

        final EncryptedTransaction newTransaction = new EncryptedTransaction(
                transactionHash,
                this.payloadEncoder.encode(encodedPayloadWithRecipients)
        );

        this.encryptedTransactionDAO.save(newTransaction);

        recipientList.forEach(recipient -> payloadPublisher.publishPayload(encodedPayloadWithRecipients, recipient));

        final byte[] key = transactionHash.getHashBytes();

        final String encodedKey = base64Decoder.encodeToString(key);

        return new SendResponse(encodedKey);
    }

    @Override
    public ResendResponse resend(ResendRequest request) {

        final byte[] publicKeyData = base64Decoder.decode(request.getPublicKey());
        PublicKey recipientPublicKey = PublicKey.from(publicKeyData);
        if (request.getType() == ResendRequestType.ALL) {

            final Collection<EncodedPayloadWithRecipients> payloads = encryptedTransactionDAO
                    .retrieveAllTransactions()
                    .stream()
                    .map(EncryptedTransaction::getEncodedPayload)
                    .map(payloadEncoder::decodePayloadWithRecipients)
                    .filter(payload -> payload.getRecipientKeys().contains(recipientPublicKey))
                    .collect(toList());

            payloads.forEach(payload
                    -> payload.getRecipientKeys().forEach(recipientKey
                            -> payloadPublisher.publishPayload(payload, recipientKey)
                    )
            );
            return new ResendResponse();
        } else {

            final byte[] hashKey = base64Decoder.decode(request.getKey());
            MessageHash messageHash = new MessageHash(hashKey);

            final EncodedPayloadWithRecipients payloadWithRecipients = encryptedTransactionDAO
                    .retrieveByHash(messageHash)
                    .map(EncryptedTransaction::getEncodedPayload)
                    .map(payloadEncoder::decodePayloadWithRecipients)
                    .map(p -> enclave.addRecipientToPayload(p, recipientPublicKey))
                    .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + messageHash + " was not found"));


            final byte[] encoded = payloadEncoder.encode(payloadWithRecipients);

            return new ResendResponse(encoded);
        }
    }

    @Override
    public void storePayload(byte[] payload) {

        final EncodedPayloadWithRecipients encodedPayloadWithRecipients = payloadEncoder.decodePayloadWithRecipients(payload);

        final MessageHash transactionHash = Optional.of(encodedPayloadWithRecipients)
                .map(EncodedPayloadWithRecipients::getEncodedPayload)
                .map(EncodedPayload::getCipherText)
                .map(messageHashFactory::createFromCipherText).get();

        final EncryptedTransaction newTransaction = new EncryptedTransaction(
                transactionHash,
                this.payloadEncoder.encode(encodedPayloadWithRecipients)
        );

        this.encryptedTransactionDAO.save(newTransaction);

        LOGGER.info(base64Decoder.encodeToString(transactionHash.getHashBytes()));
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

        final EncryptedTransaction encryptedTransaction = encryptedTransactionDAO
                .retrieveByHash(hash)
                .orElseThrow(() -> new TransactionNotFoundException("Message with hash " + hash + " was not found"));

        PublicKey recipientKey = to.map(PublicKey::from)
                .orElse(searchForRecipientKey(encryptedTransaction)
                        .orElseThrow(() -> new RuntimeException("No key found that could decrypt the requested payload: " + hash))
                );

        byte[] payload = unencryptTransaction(encryptedTransaction, recipientKey);
        String encodedPayload = base64Decoder.encodeToString(payload);
        return new ReceiveResponse(encodedPayload);

    }

    private Optional<PublicKey> searchForRecipientKey(EncryptedTransaction encryptedTransaction) {
        for (final PublicKey potentialMatchingKey : enclave.getPublicKeys()) {
            try {
                unencryptTransaction(encryptedTransaction, potentialMatchingKey);
                return Optional.of(potentialMatchingKey);
            } catch (final NaclException ex) {
                LOGGER.debug("Attempted payload decryption using wrong key, discarding.");
            }
        }
        return Optional.empty();
    }

    private byte[] unencryptTransaction(EncryptedTransaction encryptedTransaction, final PublicKey providedKey) {

        final EncodedPayloadWithRecipients payloadWithRecipients
                = payloadEncoder.decodePayloadWithRecipients(encryptedTransaction.getEncodedPayload());

        return enclave.unencryptTransaction(payloadWithRecipients, providedKey);

    }


}
