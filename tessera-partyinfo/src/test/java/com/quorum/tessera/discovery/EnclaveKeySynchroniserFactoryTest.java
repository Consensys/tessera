package com.quorum.tessera.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;

public class EnclaveKeySynchroniserFactoryTest {

  @Test
  public void provider() {
    EnclaveKeySynchroniser enclaveKeySynchroniser = EnclaveKeySynchroniserFactory.provider();
    assertThat(enclaveKeySynchroniser)
        .isNotNull()
        .isExactlyInstanceOf(EnclaveKeySynchroniserImpl.class);
  }

  @Test
  public void testCallToDelegate() {
    EnclaveKeySynchroniser enclaveKeySynchroniser = mock(EnclaveKeySynchroniser.class);
    EnclaveKeySynchroniserFactory enclaveKeySynchroniserFactory =
        new EnclaveKeySynchroniserFactory(enclaveKeySynchroniser);

    enclaveKeySynchroniserFactory.syncKeys();

    verify(enclaveKeySynchroniser).syncKeys();
  }

  @Test
  public void defaultConstructor() {
    EnclaveKeySynchroniserFactory enclaveKeySynchroniserFactory =
        new EnclaveKeySynchroniserFactory();
    assertThat(enclaveKeySynchroniserFactory).isNotNull();
  }
}
