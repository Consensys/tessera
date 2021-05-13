package com.quorum.tessera.enclave;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.Config;

public class MockEnclaveClientFactory implements EnclaveClientFactory {

  @Override
  public EnclaveClient create(Config config) {
    return mock(EnclaveClient.class);
  }
}
