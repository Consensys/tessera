package com.quorum.tessera.partyinfo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Resend batch response.
 */
@ApiModel
public class ResendBatchResponse {

    @ApiModelProperty("message count")
    private long total;

    public ResendBatchResponse() {
    }

    public ResendBatchResponse(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
