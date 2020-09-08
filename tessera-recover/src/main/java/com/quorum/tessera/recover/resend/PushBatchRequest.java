package com.quorum.tessera.recover.resend;

import java.util.List;

public interface PushBatchRequest {

    List<byte[]> getEncodedPayloads();

    static PushBatchRequest from(List<byte[]> encodedPayloads) {
        return () -> encodedPayloads;
    }
}
