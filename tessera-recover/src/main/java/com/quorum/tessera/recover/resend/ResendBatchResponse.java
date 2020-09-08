package com.quorum.tessera.recover.resend;

public interface ResendBatchResponse {

    long getTotal();

    static ResendBatchResponse from(long total) {
        return () -> total;
    }
}
