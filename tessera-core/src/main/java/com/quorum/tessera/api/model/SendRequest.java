package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlMimeType;
/**
 * Model representation of a JSON body on incoming HTTP requests
 *
 * <p>Used when a new transaction is to be created where this node is the sender
 */
@ApiModel
public class SendRequest {

    @XmlMimeType("base64Binary")
    @Size(min = 1)
    @NotNull
    @ApiModelProperty("Encrypted payload to send to other parties.")
    private byte[] payload;

    @ApiModelProperty("Sender public key")
    private String from;

    @ApiModelProperty("Recipient public keys")
    private String[] to;

    @ApiModelProperty("Privacy flag")
    private int privacyFlag;

    @ApiModelProperty("Affected contract transactions")
    private String[] affectedContractTransactions;

    @ApiModelProperty("Execution hash")
    private String execHash;

    public byte[] getPayload() {
        return this.payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(final String from) {
        this.from = from;
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(final String... to) {
        this.to = to;
    }

    public int getPrivacyFlag() {
        return privacyFlag;
    }

    public void setPrivacyFlag(int privacyFlag) {
        this.privacyFlag = privacyFlag;
    }

    public String[] getAffectedContractTransactions() {
        return affectedContractTransactions;
    }

    public void setAffectedContractTransactions(String[] affectedContractTransactions) {
        this.affectedContractTransactions = affectedContractTransactions;
    }

    public String getExecHash() {
        return execHash;
    }

    public void setExecHash(String execHash) {
        this.execHash = execHash;
    }
}
