package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private EncryptedTransactionDAO encryptedTransactionDAO;

    private PayloadEncoder payloadEncoder;

    public TransactionServiceImpl(final EncryptedTransactionDAO encryptedTransactionDAO,
                                  final PayloadEncoder payloadEncoder) {
        this.encryptedTransactionDAO = Objects.requireNonNull(encryptedTransactionDAO);
        this.payloadEncoder = Objects.requireNonNull(payloadEncoder);
    }

    @Override
    public boolean delete(final MessageHash hash) {
        return encryptedTransactionDAO.delete(hash);
    }

    @Override
    public Collection<String> retrieveAllForRecipient(final Key recipientPublicKey) {
        return null;
    }

    @Override
    public String retrievePayload(final MessageHash hash, final Key intendedRecipient) {
        return null;
    }

    @Override
    public String retrieve(final MessageHash hash, final Key sender) {
        return null;
    }

    @Override
    public MessageHash storePayloadFromOtherNode(final byte[] sealedPayload) {
        return null;
    }

    @Override
    public Map<Key, Map<byte[], byte[]>> encryptPayload(final byte[] message, final Key senderPublicKey, final Collection<Key> recipientPublicKeys) {
        return null;
    }

}
