package com.quorum.tessera.data.staging;

import com.quorum.tessera.enclave.PrivacyMode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** The JPA entity that contains the staging transaction information. */
@Entity
@Table(
        name = "ST_TRANSACTION",
        indexes = {@Index(name = "ST_TRANSACTION_VALSTG", columnList = "VALIDATION_STAGE", unique = false)})
@NamedQueries({
    // this searches for staging transactions which have not been validated yet (validation stage is null) and do not
    // depend non validated transactions
    // TODO must understand how inefficient this is... Other solutions are welcome. Outer joins fail on sqlite (the
    // generated query contains curly brackets)
    @NamedQuery(
            name = "StagingTransaction.stagingQuery",
            query =
                    "SELECT st FROM StagingTransaction st WHERE st.validationStage is null and not exists "
                            + "    (select act from StagingAffectedContractTransaction act  where act.stagingAffectedContractTransactionId.source=st.hash and  "
                            + "        (select ast.validationStage from StagingTransaction ast where ast.hash=act.stagingAffectedContractTransactionId.affected) is null"
                            + "    )")
})
public class StagingTransaction implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="ID",nullable = false,unique = true,updatable = false)
    private Long id;

    @Embedded
    @AttributeOverride(
            name = "hash",
            column = @Column(name = "HASH", nullable = false, unique = true, updatable = false))
    private MessageHashStr hash;

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

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            mappedBy = "transaction",
            orphanRemoval = true)
    @MapKey(name = "recipient")
    private Map<StagingRecipient, StagingTransactionRecipient> recipients = new HashMap<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            mappedBy = "sourceTransaction",
            orphanRemoval = true)
    @MapKey(name = "affected")
    private Map<MessageHashStr, StagingAffectedContractTransaction> affectedContractTransactions = new HashMap<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.ALL},
            mappedBy = "transaction",
            orphanRemoval = true)
    @MapKey(name = "recipient")
    private Map<StagingRecipient, StagingTransactionVersion> versions = new HashMap<>();

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

    public MessageHashStr getHash() {
        return this.hash;
    }

    public void setHash(final MessageHashStr hash) {
        this.hash = hash;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Map<StagingRecipient, StagingTransactionRecipient> getRecipients() {
        return recipients;
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

    public void setRecipients(Map<StagingRecipient, StagingTransactionRecipient> recipients) {
        this.recipients = recipients;
    }

    public Map<MessageHashStr, StagingAffectedContractTransaction> getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(
            Map<MessageHashStr, StagingAffectedContractTransaction> affectedContractTransactions) {
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

    public Map<StagingRecipient, StagingTransactionVersion> getVersions() {
        return versions;
    }

    public void setVersions(Map<StagingRecipient, StagingTransactionVersion> versions) {
        this.versions = versions;
    }

    @Override
    public int hashCode() {
        return 47 * 3 + Objects.hashCode(this.hash);
    }

    @Override
    public boolean equals(final Object obj) {

        return (obj instanceof StagingTransaction) && Objects.equals(this.hash, ((StagingTransaction) obj).hash);
    }
}
