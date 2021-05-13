package com.quorum.tessera.p2p;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.enclave.Enclave;
import com.quorum.tessera.enclave.EnclaveFactory;

public class MockEnclaveFactory implements EnclaveFactory {
  @Override
  public Enclave createLocal(Config config) {
    return mock(Enclave.class);
  }

  @Override
  public Enclave create(Config config) {
    return mock(Enclave.class);
  }
}
