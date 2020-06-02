package com.quorum.tessera.transaction;

import java.util.Arrays;

public interface ReceiveResponse {

    byte[] getUnencryptedTransactionData();


    static ReceiveResponse from(byte[] data) {
        return () -> Arrays.copyOf(data,data.length);
    }


}
