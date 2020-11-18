package com.quorum.tessera.p2p.recovery;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/** Resend batch response. */
@ApiModel
public class ResendBatchResponse {

    @ApiModelProperty("message count")
    private Long total;

    public ResendBatchResponse() {}

    public ResendBatchResponse(Long total) {
        this.total = total;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
