package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class AffectedTransactionTest {

  @Test(expected = NullPointerException.class)
  public void buildWithNullProperties() {
    AffectedTransaction.Builder.create().build();
  }

  @Test
  public void createInstance() {

    byte[] hashBytes = "SOMEBYTES".getBytes();

    EncodedPayload encodedPayload = mock(EncodedPayload.class);

    AffectedTransaction result =
        AffectedTransaction.Builder.create()
            .withPayload(encodedPayload)
            .withHash(hashBytes)
            .build();

    assertThat(result.getPayload()).isSameAs(encodedPayload);
    assertThat(result.getHash().getBytes()).isEqualTo(hashBytes);

    EqualsVerifier.forClass(AffectedTransaction.class)
        .withOnlyTheseFields("hash")
        .usingGetClass()
        .verify();
  }
}
