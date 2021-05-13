package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import java.util.Set;
import org.junit.Test;

public class SendResponseTest {

  @Test
  public void fromTransactionHash() {
    MessageHash transactionHash = mock(MessageHash.class);
    Set<PublicKey> arbitraryManagedKeys = Set.of(PublicKey.from("arbitrary key".getBytes()));
    SendResponse response =
        SendResponse.Builder.create()
            .withMessageHash(transactionHash)
            .withManagedParties(arbitraryManagedKeys)
            .withSender(PublicKey.from("sender".getBytes()))
            .build();

    assertThat(response).isNotNull();
    assertThat(response.getTransactionHash()).isSameAs(transactionHash);
    assertThat(response.getManagedParties()).isSameAs(arbitraryManagedKeys);
    assertThat(response.getSender()).isEqualTo(PublicKey.from("sender".getBytes()));
  }

  @Test(expected = NullPointerException.class)
  public void buildWithNothing() {
    SendResponse.Builder.create().withMessageHash(null).build();
  }
}
