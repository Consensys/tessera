package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class RecipientBoxTest {

  @Test
  public void createTwoRecipientBoxesThatAreEqualButNotSame() {
    byte[] data = "DATA".getBytes();

    RecipientBox recipientBox = RecipientBox.from(data);

    RecipientBox anotherInstanceWithSameData = RecipientBox.from(data);

    assertThat(recipientBox)
        .isEqualTo(anotherInstanceWithSameData)
        .hasSameHashCodeAs(anotherInstanceWithSameData)
        .isNotSameAs(anotherInstanceWithSameData);
  }

  @Test
  public void verifyEquals() {
    EqualsVerifier.forClass(RecipientBox.class).verify();
  }
}
