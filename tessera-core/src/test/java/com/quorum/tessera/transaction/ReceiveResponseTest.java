package com.quorum.tessera.transaction;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveResponseTest {

    @Test
    public void fromSomeUnencryptedTransactionData() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse result = ReceiveResponse.from(someData);

        assertThat(result.getUnencryptedTransactionData()).containsExactly(someData);
    }

}
