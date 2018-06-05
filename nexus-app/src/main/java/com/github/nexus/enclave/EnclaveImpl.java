//package com.github.nexus.enclave;
//
//import com.github.nexus.dao.EncryptedTransactionDAO;
//import com.github.nexus.enclave.keys.KeyManager;
//import com.github.nexus.enclave.keys.model.Key;
//import com.github.nexus.enclave.model.MessageHash;
//import com.github.nexus.encryption.NaclFacade;
//
//import java.util.Collection;
//import java.util.Map;
//import java.util.Objects;
//
//public class EnclaveImpl implements Enclave {
//
//    private final NaclFacade nacl;
//
//    private final KeyManager keyManager;
//
//    private final EncryptedTransactionDAO encryptedTransactionDAO;
//
//    public EnclaveImpl(final NaclFacade nacl, final KeyManager keyManager, final EncryptedTransactionDAO dao) {
//        this.nacl = Objects.requireNonNull(nacl);
//        this.keyManager = Objects.requireNonNull(keyManager);
//        this.encryptedTransactionDAO = Objects.requireNonNull(dao);
//    }
//
//    @Override
//    public boolean delete(final MessageHash hash) {
//        return encryptedTransactionDAO.delete(hash);
//    }
//
//    @Override
//    public Collection<String> retrieveAllForRecipient(final Key recipientPublicKey) {
//        return null;
//    }
//
//    @Override
//    public String retrievePayload(final MessageHash hash, final Key intendedRecipient) {
//        return null;
//    }
//
//    @Override
//    public String retrieve(final MessageHash hash, final Key sender) {
//        return null;
//    }
//
//    @Override
//    public MessageHash storePayloadFromOtherNode(final byte[] sealedPayload) {
//        return null;
//    }
//
//    @Override
//    public Map<Key, Map<byte[], byte[]>> encryptPayload(final byte[] message, final Key senderPublicKey, final Collection<Key> recipientPublicKeys) {
//        return null;
//    }
//}
