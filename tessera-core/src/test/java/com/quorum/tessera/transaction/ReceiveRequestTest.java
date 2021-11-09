package com.quorum.tessera.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.quorum.tessera.data.MessageHash;
import com.quorum.tessera.encryption.PublicKey;
import org.junit.Test;

public class ReceiveRequestTest {

  @Test(expected = NullPointerException.class)
  public void buildWithNothing() {
    ReceiveRequest.Builder.create().build();
  }

  @Test
  public void buildWithTransactionHash() {
    MessageHash messageHash = mock(MessageHash.class);
    ReceiveRequest result =
        ReceiveRequest.Builder.create().withTransactionHash(messageHash).build();

    assertThat(result).isNotNull();
    assertThat(result.getTransactionHash()).isNotNull().isSameAs(messageHash);
    assertThat(result.getRecipient()).isNotPresent();
  }

  @Test(expected = NullPointerException.class)
  public void buildOnlyWithRecipient() {
    PublicKey recipient = mock(PublicKey.class);
    ReceiveRequest.Builder.create().withRecipient(recipient).build();
  }

  @Test
  public void buildWithTransactionHashAndRecipient() {
    MessageHash messageHash = mock(MessageHash.class);
    PublicKey recipient = mock(PublicKey.class);
    ReceiveRequest result =
        ReceiveRequest.Builder.create()
            .withTransactionHash(messageHash)
            .withRecipient(recipient)
            .build();

    assertThat(result).isNotNull();
    assertThat(result.getTransactionHash()).isNotNull().isSameAs(messageHash);
    assertThat(result.getRecipient()).containsSame(recipient);
    assertThat(result.isRaw()).isFalse();
  }

  @Test
  public void buildWithRaw() {
    MessageHash messageHash = mock(MessageHash.class);
    PublicKey recipient = mock(PublicKey.class);
    ReceiveRequest req =
        ReceiveRequest.Builder.create()
            .withTransactionHash(messageHash)
            .withRecipient(recipient)
            .withRaw(true)
            .build();

    assertThat(req).isNotNull();
    assertThat(req.getTransactionHash()).isNotNull().isSameAs(messageHash);
    assertThat(req.getRecipient()).containsSame(recipient);
    assertThat(req.isRaw()).isTrue();
  }
}
