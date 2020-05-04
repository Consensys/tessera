package com.quorum.tessera.data.staging;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Base64;
import java.util.Objects;

@Entity
@Table(name = "ST_TRANSACTION_RECIPIENT"
//    ,uniqueConstraints = {
//    @UniqueConstraint(name = "StagingTransactionRecipientHashAndRecipientAreUnique",columnNames = {"HASH","RECIPIENT"})
//
//}
)
public class StagingRecipient implements Serializable {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Embedded
    @AttributeOverride(name = "hash",
        column = @Column(name = "HASH", nullable = false, updatable = false))
    private MessageHashStr messageHash;


    @ManyToOne
    @JoinColumns({@JoinColumn(name = "TXNHASH", referencedColumnName = "HASH", insertable = false, updatable = false)})
    private StagingTransaction transaction;

//    @OneToMany(mappedBy = "recipient")
//    private Set<StagingTransactionVersion> stagingTransactionVersions = new HashSet<>();

    @Basic private boolean initiator;

    @Lob
    @Column(name = "BOX", updatable = false)
    @Basic
    private byte[] box;

    @Basic
    private String recBytes;

    public StagingRecipient() {}

    public StagingRecipient(final byte[] bytes) {
        this.recBytes = Base64.getEncoder().encodeToString(bytes);
    }

    public void setBytes(final byte[] bytes) {
        this.recBytes = Base64.getEncoder().encodeToString(bytes);
    }

    public byte[] getBytes() {
        return Base64.getDecoder().decode(recBytes);
    }

    public String getRecBytes() {
        return recBytes;
    }

    public void setRecBytes(String recBytes) {
        this.recBytes = recBytes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StagingTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(StagingTransaction transaction) {
        this.transaction = transaction;
    }

    public boolean isInitiator() {
        return initiator;
    }

    public void setInitiator(boolean initiator) {
        this.initiator = initiator;
    }

    public byte[] getBox() {
        return box;
    }

    public void setBox(byte[] box) {
        this.box = box;
    }

    public MessageHashStr getMessageHash() {
        return messageHash;
    }

    public void setMessageHash(MessageHashStr messageHash) {
        this.messageHash = messageHash;
    }

//    public Set<StagingTransactionVersion> getStagingTransactionVersions() {
//        return stagingTransactionVersions;
//    }
//
//    public void setStagingTransactionVersions(Set<StagingTransactionVersion> stagingTransactionVersions) {
//        this.stagingTransactionVersions = stagingTransactionVersions;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StagingRecipient that = (StagingRecipient) o;
        return Objects.equals(messageHash, that.messageHash) &&
            Objects.equals(recBytes, that.recBytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageHash, recBytes);
    }

    @Override
    public String toString() {
        return recBytes;
    }
}
