package com.github.nexus.enclave;

import com.github.nexus.dao.EncryptedTransactionDAO;
import com.github.nexus.enclave.keys.model.Key;
import com.github.nexus.enclave.model.MessageHash;

import java.util.Collection;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class EnclaveImpl implements Enclave {

    private final EncryptedTransactionDAO encryptedTransactionDAO;

    public EnclaveImpl(final EncryptedTransactionDAO encryptedTransactionDAO) {
        this.encryptedTransactionDAO = requireNonNull(encryptedTransactionDAO,"dao cannot be null");
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

    @Override
    public byte[] store(byte[] sender, byte[][] recipients, byte[] message) {
        return new byte[0];
    }
}
