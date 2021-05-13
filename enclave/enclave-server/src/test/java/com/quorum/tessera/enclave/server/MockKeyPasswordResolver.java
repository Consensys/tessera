package com.quorum.tessera.enclave.server;

import com.quorum.tessera.cli.keypassresolver.KeyPasswordResolver;
import com.quorum.tessera.config.Config;

public class MockKeyPasswordResolver implements KeyPasswordResolver {

  private static Config seen;

  @Override
  public void resolveKeyPasswords(final Config config) {
    seen = config;
  }

  public static Config getSeen() {
    return seen;
  }
}
