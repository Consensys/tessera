package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.model.MessageHash;
import com.quorum.tessera.transaction.model.EncryptedRawTransaction;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.util.Optional;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedRawTransactionDAOImpl implements EncryptedRawTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedRawTransactionDAOImpl.class);

    @PersistenceContext(unitName = "tessera")
    private EntityManager entityManager;

    @Override
    public EncryptedRawTransaction save(final EncryptedRawTransaction entity) {
        LOGGER.debug(
                "Persisting EncryptedRawTransaction with hash {}, payload {}, key {}, nonce {} and from {}",
                entity.getHash(),
                toHexString(entity.getEncryptedPayload()),
                toHexString(entity.getEncryptedKey()),
                toHexString(entity.getNonce()),
                toHexString(entity.getSender()));

        entityManager.persist(entity);

        return entity;
    }

    @Override
    public Optional<EncryptedRawTransaction> retrieveByHash(final MessageHash hash) {
        LOGGER.info("Retrieving payload with hash {}", hash);
        return Optional.ofNullable(entityManager.find(EncryptedRawTransaction.class, hash));
    }

    @Override
    public void delete(final MessageHash hash) {
        LOGGER.info("Deleting transaction with hash {}", hash);

        entityManager.remove(retrieveByHash(hash).orElseThrow(EntityNotFoundException::new));
    }

    private String toHexString(byte[] val) {
        if (null == val) {
            return "null";
        }
        return Hex.toHexString(val);
    }
}
