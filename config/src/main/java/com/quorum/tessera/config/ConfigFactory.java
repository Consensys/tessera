package com.quorum.tessera.config;

import java.io.InputStream;
import java.util.ServiceLoader;

public interface ConfigFactory {

  Config create(InputStream configData);

  static ConfigFactory create() {
    return ServiceLoader.load(ConfigFactory.class).findFirst().get();
  }

  default Config getConfig() {
    return ConfigHolder.INSTANCE.getConfig();
  }

  // FIXME:
  // Should only be accessed in Main
  default void store(Config config) {
    ConfigHolder.INSTANCE.setConfig(config);
  }
}
