package com.quorum.tessera.cli;

import com.quorum.tessera.config.Config;
import com.quorum.tessera.config.ConfigFactory;
import java.io.InputStream;

public class MockConfigFactory implements ConfigFactory {

  @Override
  public Config create(InputStream configData) {
    Config config = new Config();

    return config;
  }
}
