package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.PrivacyMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/** The JPA entity that contains the staging transaction information. */
@Entity
@Table(
        name = "ST_TRANSACTION",
        indexes = {@Index(name = "ST_TRANSACTION_VALSTG", columnList = "VALIDATION_STAGE", unique = false)})
@NamedQueries({
    @NamedQuery(
            name = "StagingTransaction.stagingQuery",
            query =
                    "select st FROM StagingTransaction st where st.validationStage is null and not exists "
                            + "    (select act from StagingAffectedTransaction act  where act.sourceTransaction.hash = st.hash and  "
                            + "        (select ast.validationStage from StagingTransaction ast where ast.hash = act.hash) is null"
                            + "    )"),
    @NamedQuery(name = "StagingTransaction.countAll",query = "select count(st) from StagingTransaction st"),
    @NamedQuery(name="StagingTransaction.countStaged",query = "select count(st) from StagingTransaction st where st.validationStage is not null"),
    @NamedQuery(name="StagingTransaction.findAllOrderByStage",
        query = "select st from StagingTransaction st order by coalesce(st.validationStage, select max(st.validationStage)+1 from StagingTransaction st), st.hash")
})
public class StagingTransaction implements Serializable {

    @Id
    @GeneratedValue(generator = "ATOMIC_LONG",strategy=GenerationType.AUTO)
    @Column(name="ID")
    private Long id;

    @Basic
    @Column(name = "HASH", nullable = false,updatable = false)
    private String hash;

    @Lob
    @Column(name = "SENDER_KEY", nullable = false, updatable = false)
    private byte[] senderKey;

    @Lob
    @Column(name = "CIPHER_TEXT", nullable = false, updatable = false)
    private byte[] cipherText;

    @Lob
    @Column(name = "CIPHER_TEXT_NONCE", nullable = false, updatable = false)
    private byte[] cipherTextNonce;

    @Lob
    @Column(name = "RECIPIENT_NONCE", nullable = false, updatable = false)
    private byte[] recipientNonce;

    @Lob
    @Column(name = "EXEC_HASH", updatable = false)
    private byte[] execHash;

    @Column(name = "PRIVACY_MODE", updatable = false)
    @Enumerated(EnumType.ORDINAL)
    private PrivacyMode privacyMode = PrivacyMode.STANDARD_PRIVATE;

    @Column(name = "VALIDATION_STAGE")
    @Basic
    private Long validationStage;

    @Column(name = "DATA_ISSUES")
    @Basic
    private String issues;

    @Column(name = "TIMESTAMP", updatable = false)
    private long timestamp;

    @Lob
    @Column(name="RECIPIENT_KEY")
    private byte[] recipientKey;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            mappedBy = "sourceTransaction",
            orphanRemoval = true)
    private Set<StagingAffectedTransaction> affectedContractTransactions = new HashSet<>();

    @Lob
    @Column(name = "PAYLOAD")
    private byte[] payload;

    public StagingTransaction() {}

    @PrePersist
    public void onPersist() {
        this.timestamp = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHash() {
        return this.hash;
    }

    public void setHash(final String hash) {
        this.hash = hash;
    }

    public long getTimestamp() {
        return this.timestamp;
    }



    public byte[] getSenderKey() {
        return senderKey;
    }

    public void setSenderKey(byte[] senderKey) {
        this.senderKey = senderKey;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public void setCipherText(byte[] cipherText) {
        this.cipherText = cipherText;
    }

    public byte[] getCipherTextNonce() {
        return cipherTextNonce;
    }

    public void setCipherTextNonce(byte[] cipherTextNonce) {
        this.cipherTextNonce = cipherTextNonce;
    }

    public byte[] getRecipientNonce() {
        return recipientNonce;
    }

    public void setRecipientNonce(byte[] recipientNonce) {
        this.recipientNonce = recipientNonce;
    }

    public Set<StagingAffectedTransaction> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(Set<StagingAffectedTransaction> affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public byte[] getExecHash() {
        return execHash;
    }

    public void setExecHash(byte[] execHash) {
        this.execHash = execHash;
    }

    public Long getValidationStage() {
        return validationStage;
    }

    public void setValidationStage(Long validationStage) {
        this.validationStage = validationStage;
    }

    public String getIssues() {
        return issues;
    }

    public void setIssues(String issues) {
        this.issues = issues;
    }

    public PrivacyMode getPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(PrivacyMode privacyMode) {
        this.privacyMode = privacyMode;
    }



    public byte[] getRecipientKey() {
        return recipientKey;
    }

    public void setRecipientKey(byte[] recipientKey) {
        this.recipientKey = recipientKey;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(id == null) return false;
        StagingTransaction that = (StagingTransaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
