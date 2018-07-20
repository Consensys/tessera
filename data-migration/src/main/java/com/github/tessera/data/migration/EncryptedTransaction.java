package com.github.tessera.data.migration;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "ENCRYPTED_TRANSACTION")
public class EncryptedTransaction implements Serializable {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "ENC_TX_SEQ", sequenceName = "ENC_TX_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ENC_TX_SEQ")
    private Long id;

    @Lob
    @Column(name = "HASH", nullable = false, unique = true, updatable = false)
    private byte[] hash;

    @Lob
    @Column(name = "ENCODED_PAYLOAD", nullable = false)
    private byte[] encodedPayload;

    public EncryptedTransaction(final byte[] hash, final byte[] encodedPayload) {
        this.hash = hash;
        this.encodedPayload = encodedPayload;
    }

    public EncryptedTransaction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(final byte[] hash) {
        this.hash = hash;
    }

    public byte[] getEncodedPayload() {
        return encodedPayload;
    }

    public void setEncodedPayload(final byte[] encodedPayload) {
        this.encodedPayload = encodedPayload;
    }

    @Override
    public int hashCode() {
        return 47 * 3 + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(final Object obj) {

        return (obj instanceof EncryptedTransaction)
                && Objects.equals(this.id, ((EncryptedTransaction) obj).id);
    }

    public EncryptedTransaction save() {

        EntityManager entityManager = Persistence.createEntityManagerFactory("data-migration").createEntityManager();
        entityManager.getTransaction().begin();
        try {
            EncryptedTransaction saved = entityManager.merge(this);
            entityManager.getTransaction().commit();
            return saved;
        } catch (Throwable ex) {
            entityManager.getTransaction().rollback();
            throw ex;
        }

    }

}
