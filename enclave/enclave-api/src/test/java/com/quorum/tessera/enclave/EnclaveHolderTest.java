package com.quorum.tessera.enclave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class EnclaveHolderTest {

  @Test
  public void setAndGetDefaultEnclaveHolder() {
    DefaultEnclaveHolder.INSTANCE.reset();
    EnclaveHolder enclaveHolder = DefaultEnclaveHolder.INSTANCE;
    assertThat(enclaveHolder.getEnclave()).isEmpty();

    Enclave enclave = mock(Enclave.class);
    Enclave result = enclaveHolder.setEnclave(enclave);
    assertThat(result).isSameAs(enclave);

    assertThat(enclaveHolder.getEnclave()).isPresent().containsSame(enclave);
  }

  @Test(expected = IllegalArgumentException.class)
  public void thereCanBeOnlyOneStoreEnclae() {
    DefaultEnclaveHolder.INSTANCE.reset();
    EnclaveHolder enclaveHolder = DefaultEnclaveHolder.INSTANCE;
    enclaveHolder.setEnclave(mock(Enclave.class));
    enclaveHolder.setEnclave(mock(Enclave.class));
  }
}
