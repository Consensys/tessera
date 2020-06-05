package com.quorum.tessera.transaction;

import com.quorum.tessera.enclave.PrivacyMode;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ReceiveResponseTest {

    @Test
    public void fromSomeUnencryptedTransactionData() {
        byte[] someData = "SomeData".getBytes();
        ReceiveResponse result = ReceiveResponse.Builder.create()
            .withPrivacyMode(PrivacyMode.STANDARD_PRIVATE)
            .withUnencryptedTransactionData(someData).build();

        assertThat(result.getUnencryptedTransactionData()).containsExactly(someData);
        assertThat(result.getPrivacyMode()).isEqualTo(PrivacyMode.STANDARD_PRIVATE);
    }

}
