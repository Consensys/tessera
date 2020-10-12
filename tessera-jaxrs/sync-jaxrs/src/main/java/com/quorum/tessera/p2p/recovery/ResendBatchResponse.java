package com.quorum.tessera.p2p.recovery;

import io.swagger.v3.oas.annotations.media.Schema;

/** Resend batch response. */
public class ResendBatchResponse {

    @Schema(description = "count of total transactions being resent")
    private long total;

    public ResendBatchResponse() {}

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
