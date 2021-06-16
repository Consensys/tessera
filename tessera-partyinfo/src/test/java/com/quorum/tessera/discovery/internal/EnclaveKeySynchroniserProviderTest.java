package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.quorum.tessera.discovery.EnclaveKeySynchroniser;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

public class EnclaveKeySynchroniserProviderTest {

  @Test
  public void provider() {

    try (var mockedEnclave = mockStatic(Enclave.class);
        var mockedNetworkStore = mockStatic(NetworkStore.class)) {

      NetworkStore networkStore = mock(NetworkStore.class);
      mockedNetworkStore.when(NetworkStore::getInstance).thenReturn(networkStore);

      Enclave enclave = mock(Enclave.class);
      mockedEnclave.when(Enclave::create).thenReturn(enclave);

      EnclaveKeySynchroniser enclaveKeySynchroniser = EnclaveKeySynchroniserProvider.provider();
      assertThat(enclaveKeySynchroniser)
          .isNotNull()
          .isExactlyInstanceOf(EnclaveKeySynchroniserImpl.class);

      mockedEnclave.verify(Enclave::create);
      mockedEnclave.verifyNoMoreInteractions();
      mockedNetworkStore.verify(NetworkStore::getInstance);
      mockedNetworkStore.verifyNoMoreInteractions();
      verifyNoInteractions(networkStore);
      verifyNoInteractions(enclave);
    }
  }

  @Test
  public void defaultConstructor() {
    EnclaveKeySynchroniserProvider enclaveKeySynchroniserFactory =
        new EnclaveKeySynchroniserProvider();
    assertThat(enclaveKeySynchroniserFactory).isNotNull();
  }
}
