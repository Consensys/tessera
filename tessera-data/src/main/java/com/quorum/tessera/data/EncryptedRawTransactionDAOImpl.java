package com.quorum.tessera.data;

import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import java.util.Optional;

/** A JPA implementation of {@link EncryptedTransactionDAO} */
public class EncryptedRawTransactionDAOImpl implements EncryptedRawTransactionDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedRawTransactionDAOImpl.class);

    private final EntityManagerTemplate entityManagerTemplate;

    public EncryptedRawTransactionDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerTemplate = new EntityManagerTemplate(entityManagerFactory);
    }

    @Override
    public EncryptedRawTransaction save(final EncryptedRawTransaction entity) {
        LOGGER.debug(
                "Persisting EncryptedRawTransaction with hash {}, payload {}, key {}, nonce {} and from {}",
                entity.getHash(),
                toHexString(entity.getEncryptedPayload()),
                toHexString(entity.getEncryptedKey()),
                toHexString(entity.getNonce()),
                toHexString(entity.getSender()));

        return entityManagerTemplate.execute(entityManager -> {
            entityManager.persist(entity);
            return entity;
        });

    }

    @Override
    public Optional<EncryptedRawTransaction> retrieveByHash(final MessageHash hash) {
        LOGGER.debug("Retrieving payload with hash {}", hash);

        EncryptedRawTransaction encryptedRawTransaction =
            entityManagerTemplate.execute(entityManager -> entityManager.find(EncryptedRawTransaction.class, hash));

        return Optional.ofNullable(encryptedRawTransaction);
    }

    @Override
    public void delete(final MessageHash hash) {
        LOGGER.info("Deleting transaction with hash {}", hash);
        entityManagerTemplate.execute(entityManager -> {
            EncryptedRawTransaction txn = entityManager.find(EncryptedRawTransaction.class,hash);
            if(txn == null) {
                throw new EntityNotFoundException();
            }

            entityManager.createQuery("delete from EncryptedRawTransaction where hash.hashBytes = :hash")
                .setParameter("hash",hash.getHashBytes()).executeUpdate();

            return txn;
        });
    }

    private String toHexString(byte[] val) {
        if (null == val) {
            return "null";
        }
        return Hex.toHexString(val);
    }
}
