package com.quorum.tessera.transaction;

import com.quorum.tessera.api.model.StoreRawRequest;
import com.quorum.tessera.api.model.StoreRawResponse;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.RawTransaction;
import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.enclave.model.MessageHashFactory;
import com.quorum.tessera.encryption.PublicKey;
import com.quorum.tessera.transaction.model.EncryptedRawTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.Objects;

@Transactional
public class RawTransactionManagerImpl implements RawTransactionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawTransactionManagerImpl.class);

    private final Enclave enclave;

    private final EncryptedRawTransactionDAO encryptedRawTransactionDAO;

    private final MessageHashFactory messageHashFactory = MessageHashFactory.create();

    public RawTransactionManagerImpl(Enclave enclave, EncryptedRawTransactionDAO encryptedRawTransactionDAO) {
        this.enclave = Objects.requireNonNull(enclave);
        this.encryptedRawTransactionDAO = Objects.requireNonNull(encryptedRawTransactionDAO);
    }

    @Override
    public StoreRawResponse store(StoreRawRequest storeRequest) {
        RawTransaction rawTransaction = enclave.encryptRawPayload(storeRequest.getPayload(),
            storeRequest.getFrom().map(PublicKey::from).orElseGet(enclave::defaultPublicKey));
        MessageHash hash = messageHashFactory.createFromCipherText(rawTransaction.getEncryptedPayload());

        EncryptedRawTransaction encryptedRawTransaction = new EncryptedRawTransaction(hash,
            rawTransaction.getEncryptedPayload(),
            rawTransaction.getEncryptedKey(),
            rawTransaction.getNonce().getNonceBytes(),
            rawTransaction.getFrom().getKeyBytes());

        encryptedRawTransactionDAO.save(encryptedRawTransaction);

        return new StoreRawResponse(encryptedRawTransaction.getHash().getHashBytes());
    }
}
