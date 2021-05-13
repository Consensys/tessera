package com.quorum.tessera.config.cli;

import static org.mockito.Mockito.mock;

import com.quorum.tessera.config.KeyData;
import com.quorum.tessera.config.keypairs.ConfigKeyPair;

public class MockKeyDataMarshaller implements KeyDataMarshaller {

  @Override
  public KeyData marshal(ConfigKeyPair keyPair) {
    return mock(KeyData.class);
  }
}
