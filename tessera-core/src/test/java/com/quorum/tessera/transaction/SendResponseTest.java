package com.quorum.tessera.transaction;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SendResponseTest {

    @Test
    public void fromTransactionHash() {
        MessageHash transactionHash = mock(MessageHash.class);
        Set<PublicKey> arbitraryManagedKeys = Set.of(PublicKey.from("arbitrary key".getBytes()));
        SendResponse response =
                SendResponse.Builder.create()
                        .withMessageHash(transactionHash)
                        .withManagedParties(arbitraryManagedKeys)
                        .build();

        assertThat(response).isNotNull();
        assertThat(response.getTransactionHash()).isSameAs(transactionHash);
        assertThat(response.getManagedParties()).isSameAs(arbitraryManagedKeys);
    }

    @Test(expected = NullPointerException.class)
    public void buildWithNothing() {
        SendResponse.Builder.create().withMessageHash(null).build();
    }
}
