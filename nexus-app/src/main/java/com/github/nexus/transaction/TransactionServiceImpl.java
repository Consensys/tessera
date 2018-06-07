package com.github.nexus.transaction;

import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

@Transactional
public class TransactionServiceImpl implements TransactionService {

    private static final Logger LOGGER = Logger.getLogger(TransactionServiceImpl.class.getName());

    private EncryptedTransactionDAO encryptedTransactionDAO;

    public TransactionServiceImpl(final EncryptedTransactionDAO encryptedTransactionDAO) {
        this.encryptedTransactionDAO = encryptedTransactionDAO;
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
