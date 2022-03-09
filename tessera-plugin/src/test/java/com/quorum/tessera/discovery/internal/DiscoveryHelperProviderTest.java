package com.quorum.tessera.discovery.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

import com.quorum.tessera.discovery.DiscoveryHelper;
import com.quorum.tessera.discovery.NetworkStore;
import com.quorum.tessera.enclave.Enclave;
import org.junit.Test;

public class DiscoveryHelperProviderTest {

  @Test
  public void defaultConstructorForCoverage() {
    assertThat(new DiscoveryHelperProvider()).isNotNull();
  }

  @Test
  public void provider() {

    try (var mockedEnclave = mockStatic(Enclave.class);
        var mockedNetworkStore = mockStatic(NetworkStore.class)) {

      NetworkStore networkStore = mock(NetworkStore.class);
      mockedNetworkStore.when(NetworkStore::getInstance).thenReturn(networkStore);

      Enclave enclave = mock(Enclave.class);
      mockedEnclave.when(Enclave::create).thenReturn(enclave);
      DiscoveryHelper helper = DiscoveryHelperProvider.provider();
      assertThat(helper).isNotNull().isExactlyInstanceOf(DiscoveryHelperImpl.class);

      mockedEnclave.verify(Enclave::create);
      mockedEnclave.verifyNoMoreInteractions();
      mockedNetworkStore.verify(NetworkStore::getInstance);
      mockedNetworkStore.verifyNoMoreInteractions();
      verifyNoInteractions(networkStore);
      verifyNoInteractions(enclave);
    }
  }
}
