package com.quorum.tessera.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.xml.bind.annotation.XmlMimeType;
/**
 * Model representation of a JSON body on outgoing HTTP requests
 *
 * <p>Contains a Base64 encoded string that is the decrypting payload of a transaction
 */
@ApiModel
public class ReceiveResponse {

    @XmlMimeType("base64Binary")
    @ApiModelProperty("Encode response servicing receive requests")
    private byte[] payload;

    @ApiModelProperty("Privacy flag")
    private int privacyFlag;

    @ApiModelProperty("Affected contract transactions")
    private String[] affectedContractTransactions;

    @ApiModelProperty("Execution hash")
    private String execHash;

    public ReceiveResponse(
            final byte[] payload,
            final int privacyFlag,
            final String[] affectedContractTransactions,
            final String execHash) {
        this.payload = payload;
        this.privacyFlag = privacyFlag;
        this.affectedContractTransactions = affectedContractTransactions;
        this.execHash = execHash;
    }

    public ReceiveResponse() {}

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(final byte[] payload) {
        this.payload = payload;
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
